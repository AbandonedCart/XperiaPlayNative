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

/*
 * This example uses the NDK and a helper library available in the NDK called 'native app glue',
 * which is available in %NDK_ROOT/source/android/native_app_glue.  If you are new to NDK or to
 * the NativeActivity, look through the native_app_glue source to see how you should set up your
 * native app and handle callbacks and messages from Android. Note that the callbacks registered
 * in the ANativeActivity_onCreate() entry-point must return in a timely manner, as does
 * ANativeActivity_onCreate() itself.  The Native App Glue does this by creating a pipe() and 
 * synchronization objects to handle communication between the Android, the NativeActivity and
 * the game/sample logic.
 *
 * In this example, we read the 'pointer' information from touch events from both the touch-screen
 * and the touch-pad (if available).  We store their positions and state (up or down), then draw
 * the touch positions scaled to the screen.
 *
 * Although we are using hard-coded values for the touch-pad resultion, you can and should read
 * those values at runtime in java by enumerating InputDevices and finding the touchpad device.
 *
 */

#include <jni.h>
#include <errno.h>

#include <EGL/egl.h>
#include <GLES/gl.h>

#include <android/log.h>
#include <android_native_app_glue.h>

#define TAG "TouchpadNDK"
#define LOGI(...) ((void)__android_log_print( ANDROID_LOG_INFO, TAG, __VA_ARGS__ ))
#define LOGW(...) ((void)__android_log_print( ANDROID_LOG_WARN, TAG, __VA_ARGS__ ))

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
	struct android_app* app;

	int			render;
	EGLDisplay	display;
	EGLSurface	surface;
	EGLContext	context;
	int			width;
	int			height;
	int			has_focus;

	//ugly way to track touch states
	struct TOUCHSTATE touchstate_screen[64];
	struct TOUCHSTATE touchstate_pad[64];
};

/**
 * Initialize an EGL context for the current display.
 */
static
int
engine_init_display( struct ENGINE* engine )
{
	// initialize OpenGL ES and EGL
	const EGLint attribs[] = \
	{
			EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
			EGL_BLUE_SIZE, 5,
			EGL_GREEN_SIZE, 6,
			EGL_RED_SIZE, 5,
			EGL_NONE
	};

	EGLint w, h, format;
	EGLint numConfigs;
	EGLConfig config;
	EGLSurface surface;
	EGLContext context;

	EGLDisplay display = eglGetDisplay( EGL_DEFAULT_DISPLAY );

	if( display == NULL )
	{
		LOGW( "!!! NO DISPLAY !!! eglGetDisplay" );
	}

	eglInitialize( display, 0, 0 );

	/* Here, the application chooses the configuration it desires. In this
	 * sample, we have a very simplified selection process, where we pick
	 * the first EGLConfig that matches our criteria */
	eglChooseConfig( display, attribs, &config, 1, &numConfigs );

	/* EGL_NATIVE_VISUAL_ID is an attribute of the EGLConfig that is
	 * guaranteed to be accepted by ANativeWindow_setBuffersGeometry().
	 * As soon as we picked a EGLConfig, we can safely reconfigure the
	 * ANativeWindow buffers to match, using EGL_NATIVE_VISUAL_ID. */
	eglGetConfigAttrib( display, config, EGL_NATIVE_VISUAL_ID, &format );

	ANativeWindow_setBuffersGeometry( engine->app->window, 0, 0, format );

	surface = eglCreateWindowSurface( display, config, engine->app->window, NULL );
	context = eglCreateContext( display, config, NULL, NULL );

	if( eglMakeCurrent( display, surface, surface, context ) == EGL_FALSE )
	{
		LOGW("Unable to eglMakeCurrent");
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
		LOGI( "!!! NO DISPLAY !!! engine_draw_frame" );
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
		eglMakeCurrent( engine->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT );

		if( engine->context != EGL_NO_CONTEXT )
		{
			eglDestroyContext( engine->display, engine->context );
		}

		if( engine->surface != EGL_NO_SURFACE )
		{
			eglDestroySurface( engine->display, engine->surface );
		}

		eglTerminate( engine->display );
	}

	engine->render	= 0;
	engine->display	= EGL_NO_DISPLAY;
	engine->context	= EGL_NO_CONTEXT;
	engine->surface	= EGL_NO_SURFACE;
}

/**
 * Process the next input event.
 */
static
int32_t
engine_handle_input( struct android_app* app, AInputEvent* event )
{
	struct ENGINE* engine = (struct ENGINE*)app->userData;
	if( AInputEvent_getType(event) == AINPUT_EVENT_TYPE_MOTION )
	{
		engine->render		= 1;
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

/**
 * Process the next main command.
 */
static
void
engine_handle_cmd( struct android_app* app, int32_t cmd )
{
	struct ENGINE* engine = (struct ENGINE*)app->userData;
	switch( cmd )
	{
		case APP_CMD_SAVE_STATE:
			// The system has asked us to save our current state.  Do so if needed
			break;

		case APP_CMD_INIT_WINDOW:
			// The window is being shown, get it ready.
			if( engine->app->window != NULL )
			{
				engine->has_focus = 1;
				engine_init_display( engine );
				engine_draw_frame( engine );
			}
			break;

		case APP_CMD_TERM_WINDOW:
			// The window is being hidden or closed, clean it up.
			engine_term_display( engine );
			break;

		case APP_CMD_GAINED_FOCUS:
			engine->has_focus = 1;
			break;

		case APP_CMD_LOST_FOCUS:
			// When our app loses focus, we stop rendering.
			engine->render = 0;
			engine->has_focus = 0;
			//engine_draw_frame( engine );
			break;
	}
}

/**
 * This is the main entry point of a native application that is using
 * android_native_app_glue.  It runs in its own thread, with its own
 * event loop for receiving input events and doing other things (rendering).
 */
void
android_main( struct android_app* state )
{
	struct ENGINE engine;

	// Make sure glue isn't stripped.
	app_dummy();

	memset( &engine, 0, sizeof(engine) );
	state->userData		= &engine;
	state->onAppCmd		= engine_handle_cmd;
	state->onInputEvent	= engine_handle_input;
	engine.app			= state;

	if( state->savedState != NULL )
	{
		// We are starting with a previous saved state; restore from it.
	}

	// our 'main loop'
	while( 1 )
	{
		// Read all pending events.
		int ident;
		int events;
		struct android_poll_source* source;

		// If not rendering, we will block forever waiting for events.
		// If rendering, we loop until all events are read, then continue
		// to draw the next frame.
		while( (ident = ALooper_pollAll( engine.render ? 0 : -1, NULL, &events, (void**)&source) ) >= 0 )
		{
			// Process this event.
			// This will call the function pointer android_app::onInputEvent() which in our case is
			// engine_handle_input()
			if( source != NULL )
			{
				source->process( state, source );
			}

			// Check if we are exiting.
			if( state->destroyRequested != 0 )
			{
				engine_term_display( &engine );
				return;
			}
		}

		if( engine.render && engine.has_focus )
		{
			// Drawing is throttled to the screen update rate, so there
			// is no need to do timing here.
			engine_draw_frame( &engine );
		}
	}
}
