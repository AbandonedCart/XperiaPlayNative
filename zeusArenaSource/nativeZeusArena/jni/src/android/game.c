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

/*
 * Kwaak3 - Java to quake3 interface
 * Copyright (C) 2010 Roderick Colenbrander
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

#include <dlfcn.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>
#include <jni.h>
#include <errno.h>
#include <android_native_app_glue.h>
#include <time.h>
#include <unistd.h>
#include "quake_two_android_Quake2.h"
//#include "org_zeus_arena_KwaakJNI.h"
//#include "org_zeus_arena_Game.h"

#define TAG "NativeDroidArena"
#define LOGI(...) ((void)__android_log_print( ANDROID_LOG_INFO, TAG, __VA_ARGS__ ))
#define LOGW(...) ((void)__android_log_print( ANDROID_LOG_WARN, TAG, __VA_ARGS__ ))
#define LOGE(...) ((void)__android_log_print( ANDROID_LOG_ERROR, TAG, __VA_ARGS__ ))

#define EXPORT_ME __attribute__ ((visibility("default")))

static JavaVM *jVM;

typedef unsigned char BOOL;
#define FALSE 0
#define TRUE 1

//|------------------------------------------------------			NATIVE ACTIVITY			------------------------------------------------------|
static jobject		g_pActivity		= 0;
static jmethodID	javaOnNDKTouch	= 0;
static jmethodID	javaOnNDKKey	= 0;
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
	int			width;
	int			height;
	int			has_focus;
	//ugly way to track touch states
	struct TOUCHSTATE touchstate_screen[64];
	struct TOUCHSTATE touchstate_pad[64];
};

void attach(){

}

/**
 * Process the next input event.
 */
static
int32_t
engine_handle_input( struct android_app* app, AInputEvent* event )
{
	JNIEnv *jni;
	(*jVM)->AttachCurrentThread(jVM, &jni, NULL);

	struct ENGINE* engine = (struct ENGINE*)app->userData;
	if( AInputEvent_getType(event) == AINPUT_EVENT_TYPE_MOTION )
	{
		int nPointerCount	= AMotionEvent_getPointerCount( event );
		int nSourceId		= AInputEvent_getSource( event );
		int n;

		jboolean newTouch = JNI_TRUE;
		for( n = 0 ; n < nPointerCount ; ++n )
		{
			int nPointerId	= AMotionEvent_getPointerId( event, n );
			int nAction		= AMOTION_EVENT_ACTION_MASK & AMotionEvent_getAction( event );
			int nRawAction	= AMotionEvent_getAction( event );
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
			int handled = 0;
			if( jni && g_pActivity ){
				(*jni)->CallVoidMethod( jni, g_pActivity, javaOnNDKTouch, nRawAction, touchstate[nPointerId].x, touchstate[nPointerId].y, nSourceId, 0, newTouch);
			}
			newTouch = JNI_FALSE;
		}

		return 1;
	}
	else if (AInputEvent_getType(event) == AINPUT_EVENT_TYPE_KEY){
		int action = AKeyEvent_getAction(event);
		int keyCode = AKeyEvent_getKeyCode(event);
		if(jni && g_pActivity){
			if((*jni)->ExceptionCheck(jni)) {
				(*jni)->ExceptionDescribe(jni);
				(*jni)->ExceptionClear(jni);
			}
			(*jni)->CallIntMethod(jni, g_pActivity, javaOnNDKKey, action, keyCode, AKeyEvent_getMetaState(event));
		}
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
			}
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

	//setup(state);
	//JNIEnv *env;
	//(*jVM)->AttachCurrentThread(jVM, &env, NULL);

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
		while( (ident = ALooper_pollAll( 100, NULL, &events, (void**)&source) ) >= 0 )
		//while( (ident = ALooper_pollAll( 100, NULL, &events, (void**)&source) ) >= 0 )
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
				return;
			}
			usleep(17000);	//17 miliseconds
		}
	}
}

/*static
int
RegisterThis( JNIEnv* env, jobject clazz )
{
	g_pActivity = (jobject)(*env)->NewGlobalRef( env, clazz );

	return 0;
}*/
jint EXPORT_ME
JNICALL Java_quake_two_android_Quake2_RegisterThis(JNIEnv * env, jobject clazz){
	g_pActivity = (jobject)(*env)->NewGlobalRef(env, clazz);
	return 0;
}


/*static const JNINativeMethod activity_methods[] =
{
    { "RegisterThis",	"()I",	(void*)RegisterThis },
};*/
jint EXPORT_ME JNICALL
//JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM * vm, void * reserved)
{
    JNIEnv *env;
    jVM = vm;

	#ifdef DEBUG
		__android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "JNI_OnLoad called");
	#endif

    if((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
    {
        __android_log_print(ANDROID_LOG_ERROR, "Quake_JNI", "Failed to get the environment using GetEnv()");
        return -1;
    }

    const char* interface_path = "quake/two/android/Quake2";
    	jclass java_activity_class = (*env)->FindClass( env, interface_path );

    	if( !java_activity_class )
    	{
    		LOGE( "%s - Failed to get %s class reference", __FUNCTION__, interface_path );
    	}

    	/*if( (*env)->RegisterNatives( env, java_activity_class, activity_methods, NUM_METHODS(activity_methods) ) != JNI_OK )
    	{
    		LOGE( "%s - Failed to register native activity methods", __FUNCTION__ );
    		return -1;
    	}*/

    	javaOnNDKTouch	= (*env)->GetMethodID( env, java_activity_class, "OnNativeMotion", "(IIIIIZ)V");
    	if( !javaOnNDKTouch )
    	{
    		if( (*env)->ExceptionCheck( env ) )
    		{
    			LOGE("%s - GetMethodID( 'OnNativeMotion' ) threw exception!", __FUNCTION__);
    			(*env)->ExceptionClear( env );
    		}
    	}

    	javaOnNDKKey	= (*env)->GetMethodID( env, java_activity_class, "OnNativeKeyPress", "(III)V");
    	if( !javaOnNDKKey )
    	{
    		if( (*env)->ExceptionCheck( env ) )
    		{
    			LOGE("%s - GetMethodID( 'OnNativeKeyPress' ) threw exception!", __FUNCTION__);
    			(*env)->ExceptionClear( env );
    		}
    	}
    return JNI_VERSION_1_4;
}
