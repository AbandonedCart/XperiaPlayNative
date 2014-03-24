/*
 * Kwaak3
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

package org.zeus.arena;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.zeus.arena.R;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class KwaakRenderer implements GLSurfaceView.Renderer {
	private boolean mInit=false;
	private KwaakView view;
	private Game game;
	private int showKeyboard = 0;
	
	public KwaakRenderer(KwaakView view) {
		super();
		this.view = view;
		game = Persistence.getPersistence().getGame();
	}

	public void onDrawFrame(GL10 gl) {
		/* Compute a new frame. After this call completes, Android will perform a eglSwapBuffers */
		if (game.dialog.isShowing()){
			game.dialog.dismiss();
		}
		KwaakJNI.drawFrame();
		if (view.hasFocus()){
			if (showKeyboard > 0){
				game.displayKeyboard();
				showKeyboard--;
			}
		}
		view.killBrokenTouchEvents();
	}
	
	public void showKeyboard(){
		showKeyboard = 20;	//ugh.... this is terrible...
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		/* When the screen is rotated or the keyboard slides in and in other cases this is called even
		 * when the activity is not recreated or so. Prevent that we reinitialize the game engine.
		 */
		if(mInit == false)
		{
			Log.d("Quake_JAVA", "onSurfaceChanged");
			KwaakJNI.initGame(width, height);
			mInit = true;
		}
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}
	
	public void setMenuState(int state){
		view.setMenuState(state);
	}
}

