/*
 * Copyright (c) 2011, Sony Ericsson Mobile Communications AB.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sony Ericsson Mobile Communications AB nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#include <poll.h>
#include <pthread.h>
#include <sched.h>

#include <android/configuration.h>
#include <android/looper.h>
#include <android/native_activity.h>

#include <jni.h>
#include <errno.h>

#include <EGL/egl.h>
#include <GLES/gl.h>

#include <android/log.h>

#define TAG "TouchpadNDK"
#define LOGI(...) ((void)__android_log_print( ANDROID_LOG_INFO, TAG, __VA_ARGS__ ))
#define LOGW(...) ((void)__android_log_print( ANDROID_LOG_WARN, TAG, __VA_ARGS__ ))
#define LOGE(...) ((void)__android_log_print( ANDROID_LOG_ERROR, TAG, __VA_ARGS__ ))

#define APP_STATE_NONE		0
#define APP_STATE_START		1
#define APP_STATE_RESUME	2
#define APP_STATE_PAUSE		3
#define APP_STATE_STOP		4

#define LOOPER_ID_INPUT		1

#define MSG_APP_START				1
#define MSG_APP_RESUME				2
#define MSG_APP_PAUSE				3
#define MSG_APP_SAVEINSTANCESTATE	4
#define MSG_APP_STOP				5
#define MSG_APP_DESTROYED			6
#define MSG_APP_CONFIGCHANGED		7
#define MSG_APP_LOWMEMORY			8
#define MSG_WINDOW_FOCUSCHANGED		9
#define MSG_WINDOW_CREATED			10
#define MSG_WINDOW_DESTROYED		11
#define MSG_INPUTQUEUE_CREATED		12
#define MSG_INPUTQUEUE_DESTROYED	13

struct APP_MSG
{
	char	msg;
	char	arg1;
};

struct APP_INSTANCE
{
	// The application can place a pointer to its own state object
	// here if it likes.
	void*				userData;

	// The ANativeActivity object instance that this app is running in.
	ANativeActivity*	activity;

	// The current configuration the app is running in.
	AConfiguration*		config;

	// The ALooper associated with the app's thread.
	ALooper*			looper;

	// When non-NULL, this is the input queue from which the app will
	// receive user input events.
	AInputQueue*		inputQueue;
	AInputQueue*		pendingInputQueue;

	// When non-NULL, this is the window surface that the app can draw in.
	ANativeWindow*		window;
	ANativeWindow*		pendingWindow;

	//Activity's current state: APP_STATE_*
	int					activityState;
	int					pendingActivityState;

	//
	struct APP_MSG		msgQueue[512];
	int					msgQueueLength;

	//used to syncronize between callbacks and game-thread
	pthread_mutex_t		mutex;
	pthread_cond_t		cond;

	pthread_t			thread;

	int					running;
	int					destroyed;
	int					redrawNeeded;
};

/**
 * Our saved state data.
 */
struct TOUCHSTATE
{
	int		down;
	int		x;
	int		y;
};

/**
 * Shared state for our app.
 */
struct ENGINE
{
	struct APP_INSTANCE* app;

	int			render;
	EGLDisplay	display;
	EGLSurface	surface;
	EGLContext	context;
	int			width;
	int			height;

	//ugly way to track touch states
	struct TOUCHSTATE touchstate_screen[64];
	struct TOUCHSTATE touchstate_pad[64];
};



static
int
engine_init_display( struct ENGINE* engine, int native_format )
{
	// initialize OpenGL ES and EGL
	EGLint attribs[] = 
	{
		EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
		EGL_BLUE_SIZE, 5,
		EGL_GREEN_SIZE, 6,
		EGL_RED_SIZE, 5,
		EGL_NONE
	};

	if( native_format == WINDOW_FORMAT_RGBA_8888 || native_format == WINDOW_FORMAT_RGBX_8888 )
	{
		attribs[3] = 8;
		attribs[5] = 8;
		attribs[7] = 8;
	}

	EGLint w, h, format;
	EGLint numConfigs;
	EGLConfig config;
	EGLSurface surface;
	EGLContext context;

	EGLDisplay display = eglGetDisplay( EGL_DEFAULT_DISPLAY );

	if( display == EGL_NO_DISPLAY )
	{
		LOGW( "!!! NO DISPLAY !!! eglGetDisplay" );
	}

	EGLint major, minor;
	if( eglInitialize( display, &major, &minor ) == EGL_FALSE )
	{
		LOGE( "eglInitialize() failed : %d", eglGetError() );
	}

	if( eglBindAPI( EGL_OPENGL_ES_API ) == EGL_FALSE )
	{
		LOGE( "eglBindAPI() failed : %d", eglGetError() );
	}

	if( eglChooseConfig( display, attribs, &config, 1, &numConfigs ) == EGL_FALSE )
	{
		LOGE( "eglChooseConfig() failed : %d", eglGetError() );
	}

	eglGetConfigAttrib( display, config, EGL_NATIVE_VISUAL_ID, &format );

	ANativeWindow_setBuffersGeometry( engine->app->window, 0, 0, format );

	surface = eglCreateWindowSurface( display, config, engine->app->window, 0 );
	if( surface == EGL_NO_SURFACE )
	{
		LOGE( "eglCreateWindowSurface() failed : %d ( %x : %x )", eglGetError(), display, engine->app->window );
		return -1;
	}

	static EGLint contextAttrs[] =
	{
		EGL_CONTEXT_CLIENT_VERSION, 1,
		EGL_NONE
	};

	context = eglCreateContext( display, config, 0, contextAttrs );

	if( surface == EGL_NO_CONTEXT )
	{
		LOGE( "eglCreateContext() failed : %d", eglGetError() );
		return -1;
	}

	if( eglMakeCurrent( display, surface, surface, context ) == EGL_FALSE )
	{
		LOGE( "eglMakeCurrent() failed : %d", eglGetError() );
		return -1;
	}

	eglQuerySurface( display, surface, EGL_WIDTH, &w );
	eglQuerySurface( display, surface, EGL_HEIGHT, &h );

	engine->display	= display;
	engine->context	= context;
	engine->surface	= surface;
	engine->width	= w;
	engine->height	= h;

	// Initialize GL state.
	glHint( GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST );
	glDisable( GL_CULL_FACE );
	glDisable( GL_DEPTH_TEST );

	glViewport( 0, 0, w, h );
	glOrthof( 0.0f, w, 0.0f, h, -1.0f, 1.0f );

	glMatrixMode( GL_MODELVIEW );
	glLoadIdentity();

	return 0;
}


/**
 * Tear down the EGL context currently associated with the display.
 */
static
void
engine_term_display( struct ENGINE* engine )
{
	LOGI( "engine_term_display" );

	if( engine->display != EGL_NO_DISPLAY )
	{
		if( eglMakeCurrent( engine->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT ) == EGL_FALSE )
		{
			LOGE( "term eglMakeCurrent() failed : %d", eglGetError() );
		}

		if( engine->context != EGL_NO_CONTEXT )
		{
			if( eglDestroyContext( engine->display, engine->context ) == EGL_FALSE )
			{
				LOGE( "term eglDestroyContext() failed : %d", eglGetError() );
			}
		}

		if( engine->surface != EGL_NO_SURFACE )
		{
			if( eglDestroySurface( engine->display, engine->surface ) == EGL_FALSE )
			{
				LOGE( "term eglDestroySurface() failed : %d", eglGetError() );
			}
		}

		if( eglTerminate( engine->display ) == EGL_FALSE )
		{
			LOGE( "term eglTerminate() failed : %d", eglGetError() );
		}
	}

	engine->render	= 0;
	engine->display	= EGL_NO_DISPLAY;
	engine->context	= EGL_NO_CONTEXT;
	engine->surface	= EGL_NO_SURFACE;
}

static GLfloat square[] = \
{
	-25, -25,
	25, -25,
	-25, 25,
	25, 25
};

static
void
glEnable2D()
{
	int vPort[4];

	glGetIntegerv( GL_VIEWPORT, vPort );

	glMatrixMode( GL_PROJECTION );
	glPushMatrix();
	glLoadIdentity();

	glOrthof( 0, vPort[2], 0, vPort[3], -1, 1 );
	glMatrixMode( GL_MODELVIEW );
	glPushMatrix();
	glLoadIdentity();
}

static
void
glDisable2D()
{
	glMatrixMode( GL_PROJECTION );
	glPopMatrix();
	glMatrixMode( GL_MODELVIEW );
	glPopMatrix();
}

/**
 * Just the current frame in the display.
 */
static
void
engine_draw_frame( struct ENGINE* engine )
{
	if( engine->display == NULL )
	{
		//LOGI( "!!! NO DISPLAY !!! engine_draw_frame" );
		return;
	}

	int i;
	int vPort[4];
	glGetIntegerv( GL_VIEWPORT, vPort );

	// Just fill the screen with a color.
	glClearColor( 0, 0, 0, 0 );
	glClear( GL_COLOR_BUFFER_BIT );

	glShadeModel( GL_FLAT );

	glVertexPointer( 2, GL_FLOAT, 0, square );

	glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );

	glEnable2D();
	for( i = 0; i < 64; ++i )
	{
		if( engine->touchstate_screen[i].down == 0 )
			continue;

		glPushMatrix();
			glTranslatef( engine->touchstate_screen[i].x, vPort[3]-engine->touchstate_screen[i].y ,0.0f );
			glDrawArrays( GL_TRIANGLE_STRIP, 0, 4 );
		glPopMatrix();
	}

	//used to scale the values down to 0->1.0f
	static const float padx_scale = 1.0f/966.0f;
	static const float pady_scale = 1.0f/360.0f;
	glColor4f(1.0f, 1.0f, 0.0f, 1.0f);
	for( i = 0; i < 64; ++i )
	{
		if( engine->touchstate_pad[i].down == 0 )
			continue;

		glPushMatrix();
			//once the touchpad values are scaled down to 0->1 we can scale them up to the 
			//screen resolution.
			glTranslatef(	((float)vPort[2]) * (((float)engine->touchstate_pad[i].x) * padx_scale),
							((float)vPort[3]) * (((float)engine->touchstate_pad[i].y) * pady_scale),
							0.0f );
			glDrawArrays( GL_TRIANGLE_STRIP, 0, 4 );
		glPopMatrix();
	}

	glDisable2D();

	glFlush();

	eglSwapBuffers( engine->display, engine->surface );
}

static
int32_t
engine_handle_input( struct APP_INSTANCE* app, AInputEvent* event )
{
	struct ENGINE* engine = (struct ENGINE*)app->userData;
	if( AInputEvent_getType(event) == AINPUT_EVENT_TYPE_MOTION )
	{
		int nPointerCount	= AMotionEvent_getPointerCount( event );
		int nSourceId		= AInputEvent_getSource( event );
		int n;

		for( n = 0 ; n < nPointerCount ; ++n )
		{
			int nPointerId	= AMotionEvent_getPointerId( event, n );
			int nAction		= AMOTION_EVENT_ACTION_MASK & AMotionEvent_getAction( event );
			struct TOUCHSTATE *touchstate = 0;

			if( nSourceId == AINPUT_SOURCE_TOUCHPAD )
				touchstate = engine->touchstate_pad;
			else
				touchstate = engine->touchstate_screen;

			if( nAction == AMOTION_EVENT_ACTION_POINTER_DOWN || nAction == AMOTION_EVENT_ACTION_POINTER_UP )
			{
				int nPointerIndex = (AMotionEvent_getAction( event ) & AMOTION_EVENT_ACTION_POINTER_INDEX_MASK) >> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;
				nPointerId = AMotionEvent_getPointerId( event, nPointerIndex );
			}

			if( nAction == AMOTION_EVENT_ACTION_DOWN || nAction == AMOTION_EVENT_ACTION_POINTER_DOWN )
			{
				touchstate[nPointerId].down = 1;
			}
			else if( nAction == AMOTION_EVENT_ACTION_UP || nAction == AMOTION_EVENT_ACTION_POINTER_UP || nAction == AMOTION_EVENT_ACTION_CANCEL )
			{
				touchstate[nPointerId].down = 0;
			}

			if (touchstate[nPointerId].down == 1)
			{
				touchstate[nPointerId].x = AMotionEvent_getX( event, n );
				touchstate[nPointerId].y = AMotionEvent_getY( event, n );
			}
		}

		return 1;
	}
	return 0;
}

void
app_lock_queue( struct APP_INSTANCE* state )
{
	pthread_mutex_lock( &state->mutex );
}

void
app_unlock_queue( struct APP_INSTANCE* state )
{
	pthread_cond_broadcast( &state->cond );
	pthread_mutex_unlock( &state->mutex );
}

void
instance_app_main( struct APP_INSTANCE* app_instance )
{
	LOGI( "main entering." );

	struct ENGINE engine;

	memset( &engine, 0, sizeof(engine) );
	app_instance->userData	= &engine;
	engine.app				= app_instance;

	int run = 1;

	// our 'main loop'
	while( run == 1 )
	{
		// Read all pending events.
		int msg_index;
		int ident;
		int events;
		struct android_poll_source* source;

		app_lock_queue( app_instance );

		for( msg_index = 0; msg_index < app_instance->msgQueueLength; ++msg_index )
		{
			switch( app_instance->msgQueue[msg_index].msg )
			{
				case MSG_APP_START:
				{
					app_instance->activityState = app_instance->pendingActivityState;
				}
				break;
				case MSG_APP_RESUME:
				{
					app_instance->activityState = app_instance->pendingActivityState;
				}
				break;
				case MSG_APP_PAUSE:
				{
					app_instance->activityState = app_instance->pendingActivityState;
					engine.render = 0;
				}
				break;
				case MSG_APP_STOP:
				{
					app_instance->activityState = app_instance->pendingActivityState;
				}
				break;
				case MSG_APP_SAVEINSTANCESTATE:
				{
				}
				break;
				case MSG_APP_LOWMEMORY:
				{
				}
				break;
				case MSG_APP_CONFIGCHANGED:
				{
				}
				break;
				case MSG_APP_DESTROYED:
				{
					run = 0;
				}
				break;
				case MSG_WINDOW_FOCUSCHANGED:
				{
					engine.render = app_instance->msgQueue[msg_index].arg1;
				}
				break;
				case MSG_WINDOW_CREATED:
				{
					app_instance->window = app_instance->pendingWindow;

					int nWidth	= ANativeWindow_getWidth( app_instance->window );
					int nHeight	= ANativeWindow_getHeight( app_instance->window );
					int nFormat	= ANativeWindow_getFormat( app_instance->window );

					unsigned int nHexFormat = 0x00000000;
					if( nFormat == WINDOW_FORMAT_RGBA_8888 )
						nHexFormat = 0x8888;
					else if(nFormat == WINDOW_FORMAT_RGBX_8888)
						nHexFormat = 0x8880;
					else
						nHexFormat = 0x0565;

					LOGI("Window Created : Width(%d) Height(%d) Format(%04x)", nWidth, nHeight, nHexFormat);

					engine_init_display( &engine, nFormat );
					engine.render = 1;
				}
				break;
				case MSG_WINDOW_DESTROYED:
				{
					engine_term_display( &engine );
					engine.render = 0;

					app_instance->window = NULL;
				}
				break;
				case MSG_INPUTQUEUE_CREATED:
				case MSG_INPUTQUEUE_DESTROYED:
				{
					if( app_instance->inputQueue != NULL )
						AInputQueue_detachLooper( app_instance->inputQueue );

					app_instance->inputQueue = app_instance->pendingInputQueue;
					if( app_instance->inputQueue != NULL )
					{
						AInputQueue_attachLooper( app_instance->inputQueue, app_instance->looper, LOOPER_ID_INPUT, NULL, NULL );
					}
				}
				break;
			};
		}

		app_instance->msgQueueLength = 0;

		app_unlock_queue( app_instance );

		if (!run)
			break;

		// If not rendering, we will block forever waiting for events.
		// If rendering, we loop until all events are read, then continue
		// to draw the next frame.
		while( (ident = ALooper_pollAll( 0, NULL, &events, (void**)&source )) >= 0 )
		{
			if( ident == LOOPER_ID_INPUT )
			{
				AInputEvent* event = NULL;
				if (AInputQueue_getEvent( app_instance->inputQueue, &event ) >= 0)
				{
					if( AInputQueue_preDispatchEvent( app_instance->inputQueue, event ) )
						continue;

					int handled = engine_handle_input( app_instance, event );

					AInputQueue_finishEvent( app_instance->inputQueue, event, handled );
				}
			}
		}

		if( engine.render )
		{
			// Drawing is throttled to the screen update rate, so there
			// is no need to do timing here.
			engine_draw_frame( &engine );
			usleep( 16670 );
		}
		else
		{
			usleep( 100000 );
		}
	}

	LOGI( "main exiting." );
}

///////////////////////
///////////////////////
///////////////////////
///////////////////////
///////////////////////



///////////////
static
void
OnDestroy( ANativeActivity* activity )
{
	LOGI( "NativeActivity destroy: %p\n", activity );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_APP_DESTROYED;

	while( !app_instance->destroyed )
	{
		LOGI( "NativeActivity destroy waiting on app thread" );
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnStart( ANativeActivity* activity )
{
	LOGI( "NativeActivity start: %p\n", activity );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->pendingActivityState = APP_STATE_START;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_APP_START;

	while( app_instance->activityState != app_instance->pendingActivityState )
	{
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	app_instance->pendingActivityState = APP_STATE_NONE;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnResume( ANativeActivity* activity )
{
	LOGI( "NativeActivity resume: %p\n", activity );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->pendingActivityState = APP_STATE_RESUME;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_APP_RESUME;

	while( app_instance->activityState != app_instance->pendingActivityState )
	{
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	app_instance->pendingActivityState = APP_STATE_NONE;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void*
OnSaveInstanceState( ANativeActivity* activity, size_t* out_lentch )
{
	LOGI( "NativeActivity save instance state: %p\n", activity );

	return 0;
}

static
void
OnPause( ANativeActivity* activity )
{
	LOGI( "NativeActivity pause: %p\n", activity );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->pendingActivityState = APP_STATE_PAUSE;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_APP_PAUSE;

	while( app_instance->activityState != app_instance->pendingActivityState )
	{
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	app_instance->pendingActivityState = APP_STATE_NONE;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnStop( ANativeActivity* activity )
{
	LOGI( "NativeActivity stop: %p\n", activity );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->pendingActivityState = APP_STATE_STOP;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_APP_STOP;

	while( app_instance->activityState != app_instance->pendingActivityState )
	{
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	app_instance->pendingActivityState = APP_STATE_NONE;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnConfigurationChanged( ANativeActivity* activity )
{
	LOGI( "NativeActivity configuration changed: %p\n", activity );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_APP_CONFIGCHANGED;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnLowMemory( ANativeActivity* activity )
{
	LOGI( "NativeActivity low memory: %p\n", activity );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_APP_CONFIGCHANGED;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnWindowFocusChanged( ANativeActivity* activity, int focused )
{
	LOGI( "NativeActivity window focus changed: %p -- %d\n", activity, focused );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->msgQueue[ app_instance->msgQueueLength ].msg		= MSG_WINDOW_FOCUSCHANGED;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].arg1	= focused;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnNativeWindowCreated( ANativeActivity* activity, ANativeWindow* window )
{
	LOGI( "NativeActivity native window created: %p -- %p\n", activity, window );
	
	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->pendingWindow = window;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_WINDOW_CREATED;

	while( app_instance->window != app_instance->pendingWindow )
	{
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	app_instance->pendingWindow = NULL;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnNativeWindowDestroyed( ANativeActivity* activity, ANativeWindow* window )
{
	LOGI( "NativeActivity native window destroyed: %p -- %p\n", activity, window );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->pendingWindow = NULL;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_WINDOW_DESTROYED;

	while( app_instance->window != app_instance->pendingWindow )
	{
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnInputQueueCreated( ANativeActivity* activity, AInputQueue* queue )
{
	LOGI( "NativeActivity input queue created: %p -- %p\n", activity, queue );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->pendingInputQueue = queue;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_INPUTQUEUE_CREATED;

	while( app_instance->inputQueue != app_instance->pendingInputQueue )
	{
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	app_instance->pendingInputQueue = NULL;

	pthread_mutex_unlock( &app_instance->mutex );
}

static
void
OnInputQueueDestroyed(ANativeActivity* activity, AInputQueue* queue )
{
	LOGI( "NativeActivity input queue destroyed: %p -- %p\n", activity, queue );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)activity->instance;

	pthread_mutex_lock( &app_instance->mutex );

	app_instance->pendingInputQueue = NULL;
	app_instance->msgQueue[ app_instance->msgQueueLength++ ].msg = MSG_INPUTQUEUE_DESTROYED;

	while( app_instance->inputQueue != app_instance->pendingInputQueue )
	{
		pthread_cond_wait(&app_instance->cond, &app_instance->mutex);
	}

	pthread_mutex_unlock( &app_instance->mutex );
}
///////////////

static
void*
app_thread_entry( void* param )
{
	LOGI( "NativeActivity entered application thread" );

	struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)param;

	app_instance->config = AConfiguration_new();
	AConfiguration_fromAssetManager( app_instance->config, app_instance->activity->assetManager );

	//create/get a looper
	ALooper* looper			= ALooper_prepare( ALOOPER_PREPARE_ALLOW_NON_CALLBACKS );
	app_instance->looper	= looper;

	//tell the thread which created this one that we are now up and running
	pthread_mutex_lock( &app_instance->mutex );
	app_instance->running = 1;
	pthread_cond_broadcast( &app_instance->cond );
	pthread_mutex_unlock( &app_instance->mutex );


	// run the game
	instance_app_main( app_instance );


	pthread_mutex_lock( &app_instance->mutex );
	
	AConfiguration_delete(app_instance->config);
	
	if( app_instance->inputQueue != NULL )
	{
		AInputQueue_detachLooper(app_instance->inputQueue);
	}

	app_instance->destroyed = 1;

	pthread_cond_broadcast( &app_instance->cond );
	pthread_mutex_unlock( &app_instance->mutex );

	free( app_instance );

	LOGI( "NativeActivity exting application thread" );

	return NULL;
}

//
static
struct APP_INSTANCE*
app_instance_create( ANativeActivity* activity, void* saved_state, size_t saved_state_size )
{
    struct APP_INSTANCE* app_instance = (struct APP_INSTANCE*)malloc( sizeof(struct APP_INSTANCE) );
    memset(app_instance, 0, sizeof(struct APP_INSTANCE));
    app_instance->activity = activity;

    pthread_mutex_init( &app_instance->mutex, NULL );
    pthread_cond_init( &app_instance->cond, NULL );

    pthread_attr_t attr; 
    pthread_attr_init( &attr );
    pthread_attr_setdetachstate( &attr, PTHREAD_CREATE_DETACHED );
    pthread_create( &app_instance->thread, &attr, app_thread_entry, app_instance );

    // Wait for thread to start.
    pthread_mutex_lock( &app_instance->mutex );
    while( !app_instance->running )
    {
        pthread_cond_wait( &app_instance->cond, &app_instance->mutex );
    }
    pthread_mutex_unlock( &app_instance->mutex );

    return app_instance;
}

//entry point from android/nativeactivity
void
ANativeActivity_onCreate( ANativeActivity* activity, void* saved_state, size_t saved_state_size )
{
	LOGI( "NativeActivity creating: %p\n", activity );

	activity->callbacks->onDestroy					= OnDestroy;
	activity->callbacks->onStart					= OnStart;
	activity->callbacks->onResume					= OnResume;
	activity->callbacks->onSaveInstanceState		= OnSaveInstanceState;
	activity->callbacks->onPause					= OnPause;
	activity->callbacks->onStop						= OnStop;
	activity->callbacks->onConfigurationChanged		= OnConfigurationChanged;
	activity->callbacks->onLowMemory				= OnLowMemory;
	activity->callbacks->onWindowFocusChanged		= OnWindowFocusChanged;
	activity->callbacks->onNativeWindowCreated		= OnNativeWindowCreated;
	activity->callbacks->onNativeWindowDestroyed	= OnNativeWindowDestroyed;
	activity->callbacks->onInputQueueCreated		= OnInputQueueCreated;
	activity->callbacks->onInputQueueDestroyed		= OnInputQueueDestroyed;

	activity->instance = app_instance_create( activity, saved_state, saved_state_size );
}
