package quake.two.android;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

public class QuakeRenderer implements GLSurfaceView.Renderer {

	public static final int STATE_RESET=0;
	public static final int STATE_SURFACE_CREATED=1;
	public static final int STATE_RUNNING=2;
	public static final int STATE_ERROR=100;
	private int state = STATE_RESET; 
	private int counter_fps=0;
	private long tprint_fps= 0;
	private int framenum=0;
	// speed limit : 10 FPS
	private int speed_limit = 0;//40;	//100;	//200;
	private int vibration_duration = 100;	//0;
	private boolean vibration_running = false;
	private long vibration_end;
	private long tprev = 0;
	private boolean paused = false;
	private Quake2 game;
	private int width;
	private int height;
	private QuakeView view;
	private int showKeyboard = 0;

	public QuakeRenderer(Quake2 quake2, QuakeView view) {
		game = quake2;
		this.view = view;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	// deprecated ... use setEGLConfigChooser
	//public int[] getConfigSpec() {
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.d("Renderer", "onSurfaceCreated");
		switch(getState()){
		case STATE_RESET:
			setState(STATE_SURFACE_CREATED);
			break;        	 
		default:
			throw new Error("wrong state");
		}  	
		//this.gl = gl;
		//CHECK THIS:
		//AndroidRenderer.renderer.set_gl(gl);
		//AndroidRenderer.renderer.set_size(320,240);
		/*
		 * By default, OpenGL enables features that improve quality
		 * but reduce performance. One might want to tweak that
		 * especially on software renderer.
		 */
		gl.glDisable(GL10.GL_DITHER);
		/*
		 * Some one-time OpenGL initialization can be made here
		 * probably based on features of this particular context
		 */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,GL10.GL_NICEST);	//GL10.GL_FASTEST);
	}

	private void init(int width, int height){
		Log.i( "Quake2", "version : " + Quake2.Quake2GetVersion());
		Log.i( "Quake2", "screen size : " + width + "x"+ height);
		Quake2.Quake2SetWidth(width);
		Quake2.Quake2SetHeight(height);
		this.width = width;
		this.height = height;
		/*Log.d("Renderer", "init");
    	for (int k=0;k<60;k++){
    		Log.d("Renderer", "sleep "+k);
	    	try {
	 		   Thread.sleep(1000);
	 		} catch (InterruptedException e) {
	 			e.printStackTrace();
	 		}
    	}*/
		////////////////
		Log.i("Quake2", "Quake2Init start");
		int ret = Quake2.Quake2Init();
		Log.i("Quake2", "Quake2Init done");
		if (ret!=0){
			game.setError_message(String.format("initialisation error detected (code %d)\nworkaround : reinstall APK or reboot phone.", ret));
			Log.e( "Quake2", game.getError_message());
			//System.exit(1);
			setState(STATE_ERROR);
			// error, wrong thread ...
			Log.e("Quake2 error", "wrong thread");
			return;
		}
		game.setTstart(SystemClock.uptimeMillis());
	}

	//// new Renderer interface
	public void onDrawFrame(GL10 gl) {
		switch(getState()){
		case STATE_RUNNING:
			// nothing
			break;
		case STATE_ERROR:
		{
			long s = SystemClock.uptimeMillis();
			gl.glClearColor(((s>>10)&1)*1.0f,((s>>11)&1)*1.0f,((s>>12)&1)*1.0f,1.0f);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			gl.glFinish();
		}
		return;

		default:
			throw new Error("wrong state");
		}
		view.killBrokenTouchEvents();
		if (view.hasFocus()){
			if (showKeyboard > 0){
				game.displayKeyboard();
				showKeyboard--;
			}
		}
		long tnow = SystemClock.uptimeMillis(); 
		int tdelta = (int)(tnow-tprev);
		if (tprev == 0){
			tdelta = 0;
		}
		tprev = tnow;
		if (game.getTimelimit()!=0 && (tnow-game.getTstart())>= game.getTimelimit()){
			Log.i( "Quake2.java", "Timer expired. exiting");
			game.finish();
			game.setTimelimit(0);
		}
		// compute FPS
		if ((tnow-tprint_fps) >= 1000){
			if (game.isDebug()){
				Log.i("Quake2",String.format( "FPS= %d",counter_fps));
			}
			tprint_fps = tnow;
			counter_fps = 0;        	
		}
		counter_fps ++;
		// dissmiss loading dialog after some time
		if (game.getPd_loading()!=null){
			if (Quake2.Quake2GetDisableScreen()==0){
				game.getPd_loading().dismiss();
				game.setPd_loading(null);
				// restore focus _ NOT HERE ...
				//mGLSurfaceView.requestFocus();

				// start audio thread
				if (game.isEnable_audio()){
					new Thread( new Runnable(){
						public void run(){
							try {
								game.audio_thread();
							}
							catch (IOException e){
								e.printStackTrace();
							}		
						}
					}).start();
				}
			}
		}
		/*
		 * Usually, the first thing one might want to do is to clear
		 * the screen. The most efficient way of doing this is to use
		 * glClear().
		 */
		int vibration = 0;
		Quake2.Quake2SetOverlay(game.getOverlay());
		//Log.i("Quake2", "Quake2Frame start");
		//if (framenum < 30)
		//	Log.i("Quake2", String.format("frame %d",framenum));
		game.getmGLSurfaceView().kbdUpdate();
		//view.look(tdelta);
		//Quake2.Quake2MoveEvent(2, 0, 0, 0, view.getPitch(), view.getYaw(), view.getRoll());
		//view.setYaw(0);
		//view.setPitch(0);
		//game.moveUpdate(tdelta);
		while(Quake2.sQuake2Frame()==0);
		framenum ++;
		if (game.isEnable_vibrator()){
			vibration = Quake2.Quake2GetVibration();
		}
		/*     
        boolean _paused = Quake2Paused() != 0;
        if ( paused != _paused ){
            Log.i("Quake2", "Quake2Paused "+_paused);
            paused = _paused;
        }
		 */
		//Log.i("Quake2", "Quake2Frame done");
		long tafter = SystemClock.uptimeMillis(); 
		if (vibration_running && (tafter -vibration_end)> 0){
			vibration_running = false;    
		}
		if (!vibration_running && vibration == 1 && vibration_duration > 0 ){
			// Start the vibration
			game.getVibrator().vibrate(vibration_duration);
			vibration_running = true;
			vibration_end = tafter + vibration_duration;
		}
		// speed limit : 10 FPS
		// probably a bad idea, because Android will try to run 
		// other processes in the background if we go to sleep ..
		if (getSpeed_limit()>0){
			long tsleep = getSpeed_limit() - (tafter - tnow);
			if (tsleep > 0){
				SystemClock.sleep(tsleep);
			}
		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.d("Renderer", String.format("onSurfaceChanged %dx%d", width,height) );
		//AndroidRenderer.renderer.set_gl(gl);
		//AndroidRenderer.renderer.set_size(width,height);
		gl.glViewport(0, 0, width, height);
		switch(getState()){
		case STATE_SURFACE_CREATED:
			init(width, height);
			setState(STATE_RUNNING);
			break;
		case STATE_RUNNING:
			//nothing
			break;
		default:
			throw new Error("wrong state");
		}
		/*
		 * Set our projection matrix. This doesn't have to be done
		 * each time we draw, but usually a new projection needs to
		 * be set when the viewport is resized.
		 */
		/*
         float ratio = (float) width / height;
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
         gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		 */
	}
	
	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}
	public void setSpeed_limit(int speed_limit) {
		this.speed_limit = speed_limit;
	}
	public int getSpeed_limit() {
		return speed_limit;
	}

	public void showKeyboard(){
		showKeyboard = 20;	//ugh.... this is terrible...
	}

} // end of QuakeRenderer
