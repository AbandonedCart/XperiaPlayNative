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
#include "org_zeus_arena_KwaakJNI.h"
#include "org_zeus_arena_Game.h"

#define TAG "NativeDroidArena"
#define LOGI(...) ((void)__android_log_print( ANDROID_LOG_INFO, TAG, __VA_ARGS__ ))
#define LOGW(...) ((void)__android_log_print( ANDROID_LOG_WARN, TAG, __VA_ARGS__ ))
#define LOGE(...) ((void)__android_log_print( ANDROID_LOG_ERROR, TAG, __VA_ARGS__ ))

#undef NUM_METHODS
#define NUM_METHODS(x) (sizeof(x)/sizeof(*(x)))

/* Function pointers to Quake3 code */
int  (*q3main)(int argc, char **argv);
void (*drawFrame)();
void (*queueKeyEvent)(int key, int state);
void (*queueMotionEvent)(int action, float x, float y, float pressure);
void (*queueTouchEvent)(int action, float x, float y, float pressure);
void (*queueTouchpadEvent)(int action, float x, float y);
void (*queueTrackballEvent)(int action, float x, float y);
void (*requestAudioData)();
void (*setAudioCallbacks)(void *func, void *func2, void *func3);
void (*setResolution)(int width, int height);
void (*setInputCallbacks)(void *func);

/* Callbacks to Android */
jmethodID android_getPos;
jmethodID android_initAudio;
jmethodID android_writeAudio;
jmethodID android_setMenuState;

/* Containts the path to /data/data/(package_name)/libs */
static char* lib_dir=NULL;

static JavaVM *jVM;
static jboolean audioEnabled=1;
static jboolean benchmarkEnabled=0;
static jboolean lightmapsEnabled=0;
static jboolean pureServersEnabled=0;
static jboolean openArenaProtocolEnabled=0;
static jboolean showFramerateEnabled=0;
static jobject audioBuffer=0;
static jobject kwaakAudioObj=0;
static jobject kwaakRendererObj=0;
static void *libdl;
static int init=0;

typedef unsigned char BOOL;
#define FALSE 0
#define TRUE 1

//#define DEBUG

static BOOL neon_support()
{
    char buf[80];
    FILE *fp = fopen("/proc/cpuinfo", "r");
    if(!fp)
    {
        __android_log_print(ANDROID_LOG_DEBUG, "Quake", "Unable to open /proc/cpuinfo\n");
        return FALSE;
    }

    while(fgets(buf, 80, fp) != NULL)
    {
        char *features = strstr(buf, "Features");

        if(features)
        {
            char *feature;
            features += strlen("Features");
            feature = strtok(features, ": ");
            while(feature)
            {
                if(!strcmp(feature, "neon"))
                    return TRUE;

                feature = strtok(NULL, ": ");
            }
            return FALSE;
        }
    }
    return FALSE;
}

const char *get_quake3_library()
{
    /* We ship a library with Neon FPU support. This boosts performance a lot but it only works on a few CPUs. */
    if(neon_support())
        return "libquake3_neon.so";

    return "libquake3.so";
}

void get_quake3_library_path(char *path)
{
    const char *libquake3 = get_quake3_library();
    if(lib_dir)
    {
        sprintf(path, "%s/%s", lib_dir, libquake3);
    }
    else
    {
        __android_log_print(ANDROID_LOG_ERROR, "Quake_JNI", "Library path not set, trying /data/data/org.zeus.arena/lib");
        sprintf(path, "/data/data/org.zeus.arena/lib/%s", libquake3);
    }
}

static void load_libquake3()
{
    char libquake3_path[80];
    get_quake3_library_path(libquake3_path);

#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake", "Attempting to load %s\n", libquake3_path);
#endif

    libdl = dlopen(libquake3_path, RTLD_NOW);
    if(!libdl)
    {
        __android_log_print(ANDROID_LOG_ERROR, "Quake", "Unable to load libquake3.so: %s\n", dlerror());
        return;
    }

    q3main = dlsym(libdl, "main");
    drawFrame = dlsym(libdl, "nextFrame");
    queueKeyEvent = dlsym(libdl, "queueKeyEvent");
    queueMotionEvent = dlsym(libdl, "queueMotionEvent");
    dlerror();
    queueTouchpadEvent = dlsym(libdl,"queueTouchpadEvent");
    LOGE("Error in loading shared lib: %s",dlerror());
    queueTrackballEvent = dlsym(libdl, "queueTrackballEvent");
    requestAudioData = dlsym(libdl, "requestAudioData");
    setAudioCallbacks = dlsym(libdl, "setAudioCallbacks");
    setResolution = dlsym(libdl, "setResolution");
    setInputCallbacks = dlsym(libdl, "setInputCallbacks");
    queueTouchEvent = dlsym(libdl,"queueTouchEvent");
    init=1;
}

int getPos()
{
    JNIEnv *env;
    (*jVM)->GetEnv(jVM, (void**) &env, JNI_VERSION_1_4);
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "getPos");
#endif
    return (*env)->CallIntMethod(env, kwaakAudioObj, android_getPos);
}

void initAudio(void *buffer, int size)
{
    JNIEnv *env;
    jobject tmp;
    (*jVM)->GetEnv(jVM, (void**) &env, JNI_VERSION_1_4);
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "initAudio");
#endif
    tmp = (*env)->NewDirectByteBuffer(env, buffer, size);
    audioBuffer = (jobject)(*env)->NewGlobalRef(env, tmp);

    if(!audioBuffer) __android_log_print(ANDROID_LOG_ERROR, "Quake_JNI", "yikes, unable to initialize audio buffer");

    return (*env)->CallVoidMethod(env, kwaakAudioObj, android_initAudio);
}

void writeAudio(int offset, int length)
{
    JNIEnv *env;
    (*jVM)->GetEnv(jVM, (void**) &env, JNI_VERSION_1_4);
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "writeAudio audioBuffer=%p offset=%d length=%d", audioBuffer, offset, length);
#endif

    (*env)->CallVoidMethod(env, kwaakAudioObj, android_writeAudio, audioBuffer, offset, length);
}

void setMenuState(int state){
	JNIEnv *env;
	(*jVM)->GetEnv(jVM, (void**) &env, JNI_VERSION_1_4);
	#ifdef DEBUG
		__android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "setMenuState state=%d", state);
	#endif

    (*env)->CallVoidMethod(env, kwaakRendererObj, android_setMenuState, state);
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_enableAudio(JNIEnv *env, jclass c, jboolean enable)
{
    audioEnabled = enable;
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_enableBenchmark(JNIEnv *env, jclass c, jboolean enable)
{
    benchmarkEnabled = enable;
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_enableLightmaps(JNIEnv *env, jclass c, jboolean enable)
{
    lightmapsEnabled = enable;
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_enablePureServers(JNIEnv *env, jclass c, jboolean enable)
{
    pureServersEnabled = enable;
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_enableOpenArenaProtocol(JNIEnv *env, jclass c, jboolean enable){
	openArenaProtocolEnabled = enable;
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_setRenderer(JNIEnv *env, jclass c, jobject obj){
    kwaakRendererObj = obj;
    jclass kwaakRendererClass;

    (*jVM)->GetEnv(jVM, (void**) &env, JNI_VERSION_1_4);
    kwaakRendererObj = (jobject)(*env)->NewGlobalRef(env, obj);
    kwaakRendererClass = (*env)->GetObjectClass(env, kwaakRendererObj);

	android_setMenuState = (*env)->GetMethodID(env,kwaakRendererClass,"setMenuState","(I)V");
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_showFramerate(JNIEnv *env, jclass c, jboolean enable)
{
    showFramerateEnabled = enable;
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_setAudio(JNIEnv *env, jclass c, jobject obj)
{
    kwaakAudioObj = obj;
    jclass kwaakAudioClass;

    (*jVM)->GetEnv(jVM, (void**) &env, JNI_VERSION_1_4);
    kwaakAudioObj = (jobject)(*env)->NewGlobalRef(env, obj);
    kwaakAudioClass = (*env)->GetObjectClass(env, kwaakAudioObj);

    android_getPos = (*env)->GetMethodID(env,kwaakAudioClass,"getPos","()I");
    android_initAudio = (*env)->GetMethodID(env,kwaakAudioClass,"initAudio","()V");
    android_writeAudio = (*env)->GetMethodID(env,kwaakAudioClass,"writeAudio","(Ljava/nio/ByteBuffer;II)V");
}


JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_initGame(JNIEnv *env, jclass c, jint width, jint height)
{
    char *argv[6];
    int argc=0;

    /* TODO: integrate settings with quake3, right now there is no synchronization */

    if(!audioEnabled)
    {
        argv[argc] = strdup("+set s_initsound 0");
        argc++;
    }

    if(lightmapsEnabled)
        argv[argc] = strdup("+set r_vertexlight 0");
    else
        argv[argc] = strdup("+set r_vertexlight 1");
    argc++;

    if(showFramerateEnabled)
        argv[argc] = strdup("+set cg_drawfps 1");
    else
        argv[argc] = strdup("+set cg_drawfps 0");
    argc++;

    if(pureServersEnabled){
    	argv[argc] = strdup("+set sv_pure 1");
    }
    else{
    	argv[argc] = strdup("+set sv_pure 0");
    }
    argc++;

    if (openArenaProtocolEnabled){
    	argv[argc] = strdup("+set protocol 71");
    	argc++;
    }

    if(benchmarkEnabled)
    {
        argv[argc] = strdup("+demo four +timedemo 1");
        argc++;
    }

#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "initGame(%d, %d)", width, height);
#endif

    setAudioCallbacks(&getPos, &writeAudio, &initAudio);
    setInputCallbacks(&setMenuState);
    setResolution(width, height);

    /* In the future we might want to pass arguments using argc/argv e.g. to start a benchmark at startup, to load a mod or whatever */
    q3main(argc, argv);
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_drawFrame(JNIEnv *env, jclass c)
{
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "nextFrame()");
#endif
    if(drawFrame) drawFrame();
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_queueKeyEvent(JNIEnv *env, jclass c, jint key, jint state)
{
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "queueKeyEvent(%d, %d)", key, state);
#endif
    if(queueKeyEvent) queueKeyEvent(key, state);
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_queueMotionEvent(JNIEnv *env, jclass c, jint action, jfloat x, jfloat y, jfloat pressure)
{
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "queueMotionEvent(%d, %f, %f, %f)", action, x, y, pressure);
#endif
    if(queueMotionEvent){
    	queueMotionEvent(action, x, y, pressure);
    }
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_queueTouchEvent(JNIEnv *env, jclass c, jint action, jfloat x, jfloat y, jfloat pressure)
{
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "queueTouchEvent(%d, %f, %f, %f)", action, x, y, pressure);
#endif
    if(queueTouchEvent) {
    	//__android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "queueTouchEvent(%d, %f, %f)", action, x, y);
    	queueTouchEvent(action, x, y, pressure);
    }
    else{
    	__android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "queueTouchEvent not found");
    }
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_queueTouchpadEvent(JNIEnv *env, jclass c, jint action, jfloat x, jfloat y){
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "queueTouchpadEvent(%d, %f, %f)", action, x, y);
#endif
    if(queueTouchpadEvent){
    	queueTouchpadEvent(action, x, y);
    }
    else{
    	__android_log_print(ANDROID_LOG_ERROR, "Quake_JNI", "queueTouchpadEvent not found");
    }
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_queueTrackballEvent(JNIEnv *env, jclass c, jint action, jfloat x, jfloat y)
{
#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "queueTrackballEvent(%d, %f, %f)", action, x, y);
#endif
    if(queueTrackballEvent) queueTrackballEvent(action, x, y);
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_requestAudioData(JNIEnv *env, jclass c)
{
    if(requestAudioData) requestAudioData();
}

JNIEXPORT void JNICALL Java_org_zeus_arena_KwaakJNI_setLibraryDirectory(JNIEnv *env, jclass c, jstring jpath)
{
    jboolean iscopy;
    const jbyte *path = (*env)->GetStringUTFChars(env, jpath, &iscopy);
    lib_dir = strdup(path);
    (*env)->ReleaseStringUTFChars(env, jpath, path);

#ifdef DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, "Quake_JNI", "path=%s\n", lib_dir);
#endif
}

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
				touchstate[nPointerId].x = AMotionEvent_getX(event, n);
				touchstate[nPointerId].y = AMotionEvent_getY(event, n);
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
	while(1){
		// Read all pending events.
		int ident;
		int events;
		struct android_poll_source* source;

		// If not rendering, we will block forever waiting for events.
		// If rendering, we loop until all events are read, then continue
		// to draw the next frame.
		while((ident = ALooper_pollAll(100, NULL, &events, (void**)&source)) >= 0)
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
JNIEXPORT jint JNICALL Java_org_zeus_arena_Game_RegisterThis( JNIEnv* env, jobject clazz )
{
	g_pActivity = (jobject)(*env)->NewGlobalRef( env, clazz );

	return 0;
}


/*static const JNINativeMethod activity_methods[] =
{
    { "RegisterThis",	"()I",	(void*)RegisterThis },
};*/

JNIEXPORT jint JNICALL
JNI_OnLoad( JavaVM * vm, void * reserved )
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

    if(!init){
    	load_libquake3();
    }
    const char* interface_path = "org/zeus/arena/Game";
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

    javaOnNDKTouch = (*env)->GetMethodID( env, java_activity_class, "OnNativeMotion", "(IIIIIZ)V");
    if( !javaOnNDKTouch )
    {
    	if( (*env)->ExceptionCheck( env ) )
    	{
    		LOGE("%s - GetMethodID( 'OnNativeMotion' ) threw exception!", __FUNCTION__);
    		(*env)->ExceptionClear( env );
    	}
    }

    javaOnNDKKey = (*env)->GetMethodID( env, java_activity_class, "OnNativeKeyPress", "(III)V");
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
