/*
 Copyright (C) 1997-2001 Id Software, Inc.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */

#include <termios.h>
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <stdarg.h>
#include <stdio.h>
#include <signal.h>
#include <sys/mman.h>

#if defined (__linux__)
#include <asm/io.h>
#include <sys/vt.h>
#endif

#include "../ref_soft/r_local.h"
#include "../client/keys.h"
#include "rw_android.h"
#include "quake2-jni.h"

#include <android/log.h>

/*****************************************************************************/
/* KEYBOARD                                                                  */
/*****************************************************************************/

static Key_Event_fp_t Key_Event_fp;



#define MAX_KEY_EVENTS 128

static int keyEvents[MAX_KEY_EVENTS];
static int eventId ;

void KBD_Init(Key_Event_fp_t fp) {
    //__android_log_print(ANDROID_LOG_DEBUG, "KBD", "KBD_Init\n");
    Key_Event_fp = fp;
    eventId = 0;
}

        
void KBD_Update(void) {
	
	int k;
	
    //__android_log_print(ANDROID_LOG_DEBUG, "rw_in_android.c", "KBD_Update\n");
	
	for (k=0;k<eventId;k++){
		int event = keyEvents[k];
	                	           
	    int key = event & 0x00ff;
	    int down = (event>>8) & 0x00ff;
	                	
		Key_Event_fp(key, down);
	}
	
	eventId = 0;
}



void quake2_jni_key_event( int key, int down) 
{

	if ( eventId < MAX_KEY_EVENTS )
		keyEvents[eventId++] = key | down<<8;

}


void KBD_Close(void) {

}

/*****************************************************************************/
/* MOUSE                                                                     */
/*****************************************************************************/


static in_state_t *in_state;


static struct {
	int mode;
	int forwardmove, sidemove, upmove;
	float pitch, yaw, roll;
} event;


	
// this is inside the renderer shared lib, so these are called from vid_so


void RW_IN_Init(in_state_t *in_state_p) {
	
    in_state = in_state_p;

	event.mode = 0;
	
    //__android_log_print(ANDROID_LOG_DEBUG, "KBD", "RW_IN_Init\n");
}

void RW_IN_Shutdown(void) {
    //__android_log_print(ANDROID_LOG_DEBUG, "KBD", "RW_IN_Shutdown\n");
}

/*
 ===========
 IN_Commands
 ===========
 */
void RW_IN_Commands(void) {
    //__android_log_print(ANDROID_LOG_DEBUG, "KBD", "RW_IN_Commands\n");
}

/*
 ===========
 IN_Move
 ===========
 */




	
void quake2_jni_move_event( int mode, 
				int forwardmove, int sidemove, int upmove,
				float pitch, float yaw, float roll
				) {
	
	/*
	__android_log_print(ANDROID_LOG_DEBUG, "rw_in_android.c", 
					"quake2_jni_move_event forwardmove= %d yaw= %.0f\n", forwardmove, yaw);
*/
	event.mode = mode;
	
	event.forwardmove = forwardmove;
	event.sidemove = sidemove;
	event.upmove = upmove;

	
	event.pitch = pitch;
	event.yaw = yaw;
	event.roll = roll;	
	
	
	
}


void RW_IN_Move(usercmd_t *cmd) {
	
    //__android_log_print(ANDROID_LOG_DEBUG, "rw_in_android.c", "RW_IN_Move\n");

	if ( event.mode == 1 ){ // delta mode
	/*
		__android_log_print(ANDROID_LOG_DEBUG, "rw_in_android.c", 
				"RW_IN_Move forwardmove= %d yaw= %.0f\n", event.forwardmove, event.yaw);
		*/
		
		cmd->forwardmove += event.forwardmove;
		cmd->sidemove += event.sidemove;
		cmd->upmove += event.upmove;
		
		in_state->viewangles[PITCH] += event.pitch;
		in_state->viewangles[YAW] += event.yaw ;
		in_state->viewangles[ROLL] += event.roll ;

	} else if ( event.mode == 2 ){
		
		cmd->forwardmove += event.forwardmove;
		cmd->sidemove += event.sidemove;
		cmd->upmove += event.upmove;
		
		in_state->viewangles[PITCH] = event.pitch;
		in_state->viewangles[YAW] += event.yaw ;
		in_state->viewangles[ROLL] += event.roll ;
	}
	
	event.mode = 0; // no event 
	
	

}

void RW_IN_Frame(void) {
    //__android_log_print(ANDROID_LOG_DEBUG, "KBD", "RW_IN_Frame\n");
}

void RW_IN_Activate(void) {
    //__android_log_print(ANDROID_LOG_DEBUG, "KBD", "RW_IN_Activate\n");
}

