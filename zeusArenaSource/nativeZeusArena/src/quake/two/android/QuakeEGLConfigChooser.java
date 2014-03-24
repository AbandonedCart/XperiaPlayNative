package quake.two.android;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.util.Log;

public class QuakeEGLConfigChooser implements EGLConfigChooser {

/*
 need the good config for hardware acceleration

 on HTC G2 (RGB 565 screen) default EGLConfigChooser is ok
 on Samsung Galaxy (RGB 888 screen) default EGLConfigChooser is NOT hardware accelerated

 GL strings when hardware accelerated :

D/libquake2.so( 9520): GL_VENDOR: QUALCOMM, Inc.
D/libquake2.so( 9520): GL_RENDERER: Q3Dimension MSM7500 01.02.08 0 4.0.0
D/libquake2.so( 9520): GL_VERSION: OpenGL ES 1.0-CM
D/libquake2.so( 9520): GL_EXTENSIONS: GL_ARB_texture_env_combine GL_ARB_texture_env_crossbar GL_ARB_texture_env_dot3 GL_ARB_texture_mirrored_repeat GL_ARB_vertex_buffer_object GL_ATI_extended_texture_coordinate_data_formats GL_ATI_imageon_misc GL_ATI_texture_compression_atitc GL_EXT_blend_equation_separate GL_EXT_blend_func_separate GL_EXT_blend_minmax GL_EXT_blend_subtract GL_EXT_stencil_wrap GL_OES_byte_coordinates GL_OES_compressed_paletted_texture GL_OES_draw_texture GL_OES_fixed_point GL_OES_matrix_palette GL_OES_point_size_array GL_OES_point_sprite GL_OES_read_format GL_OES_single_precision GL_OES_vertex_buffer_object GL_QUALCOMM_vertex_buffer_object GL_QUALCOMM_direct_texture  EXT_texture_env_add

all configs available :

I/Quake2.java(11435): numConfigs=22
I/Quake2.java(11435): config= EGLConfig rgba=5650 depth=16 stencil=0 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=5551 depth=16 stencil=0 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=4444 depth=16 stencil=0 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=5650 depth=16 stencil=4 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=5551 depth=16 stencil=4 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=4444 depth=16 stencil=4 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=5650 depth=0 stencil=0 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=5551 depth=0 stencil=0 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=4444 depth=0 stencil=0 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=5650 depth=0 stencil=4 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=5551 depth=0 stencil=4 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=4444 depth=0 stencil=4 native=0 buffer=16 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=8888 depth=16 stencil=0 native=0 buffer=32 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=8888 depth=16 stencil=4 native=0 buffer=32 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=8888 depth=0 stencil=0 native=0 buffer=32 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=8888 depth=0 stencil=4 native=0 buffer=32 caveat=0x3038
I/Quake2.java(11435): config= EGLConfig rgba=5650 depth=0 stencil=0 native=1 buffer=16 caveat=0x3050
I/Quake2.java(11435): config= EGLConfig rgba=5650 depth=16 stencil=0 native=1 buffer=16 caveat=0x3050
I/Quake2.java(11435): config= EGLConfig rgba=8888 depth=0 stencil=0 native=1 buffer=32 caveat=0x3050
I/Quake2.java(11435): config= EGLConfig rgba=8888 depth=16 stencil=0 native=1 buffer=32 caveat=0x3050
I/Quake2.java(11435): config= EGLConfig rgba=0008 depth=0 stencil=0 native=1 buffer=8 caveat=0x3050
I/Quake2.java(11435): config= EGLConfig rgba=0008 depth=16 stencil=0 native=1 buffer=8 caveat=0x3050*/
	private Quake2 game;

	public QuakeEGLConfigChooser(Quake2 quake2) {
		game = quake2;
	}

	public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
		Log.i( "Quake2.java", "chooseConfig");
		int[] mConfigSpec  = {
				//EGL10.EGL_RED_SIZE, 8,
				//EGL10.EGL_GREEN_SIZE, 8,
				//EGL10.EGL_BLUE_SIZE, 8,
				//EGL10.EGL_ALPHA_SIZE, 0,
				EGL10.EGL_DEPTH_SIZE, 16,
				//EGL10.EGL_STENCIL_SIZE, 0,
				EGL10.EGL_NONE};
		int[] num_config = new int[1];
		egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config);
		int numConfigs = num_config[0];
		Log.i( "Quake2.java", "numConfigs="+numConfigs);
		if (numConfigs <= 0) {
			throw new IllegalArgumentException(
			"No EGL configs match configSpec");
		}
		EGLConfig[] configs = new EGLConfig[numConfigs];
		egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs, num_config);
		if (game.isDebug())
			for(EGLConfig config : configs) {
				Log.i( "Quake2.java", "found EGL config : " + printConfig(egl,display,config));       	
			}
		// best choice : select first config
		Log.i( "Quake2.java", "selected EGL config : " + printConfig(egl,display,configs[0]));
		return configs[0];
	}


	private  String printConfig(EGL10 egl, EGLDisplay display,
			EGLConfig config) {
		int r = findConfigAttrib(egl, display, config,
				EGL10.EGL_RED_SIZE, 0);
		int g = findConfigAttrib(egl, display, config,
				EGL10.EGL_GREEN_SIZE, 0);
		int b = findConfigAttrib(egl, display, config,
				EGL10.EGL_BLUE_SIZE, 0);
		int a = findConfigAttrib(egl, display, config,
				EGL10.EGL_ALPHA_SIZE, 0);
		int d = findConfigAttrib(egl, display, config,
				EGL10.EGL_DEPTH_SIZE, 0);
		int s = findConfigAttrib(egl, display, config,
				EGL10.EGL_STENCIL_SIZE, 0);
		/*
		 * 
		 * EGL_CONFIG_CAVEAT value 

     #define EGL_NONE		       0x3038	
     #define EGL_SLOW_CONFIG		       0x3050	
     #define EGL_NON_CONFORMANT_CONFIG      0x3051	
		 */

		return String.format("EGLConfig rgba=%d%d%d%d depth=%d stencil=%d", r,g,b,a,d,s)
		+ " native=" + findConfigAttrib(egl, display, config, EGL10.EGL_NATIVE_RENDERABLE, 0)
		+ " buffer=" + findConfigAttrib(egl, display, config, EGL10.EGL_BUFFER_SIZE, 0)
		+ String.format(" caveat=0x%04x" , findConfigAttrib(egl, display, config, EGL10.EGL_CONFIG_CAVEAT, 0))
		;
	}

	private int findConfigAttrib(EGL10 egl, EGLDisplay display,
			EGLConfig config, int attribute, int defaultValue) {
		int[] mValue = new int[1];
		if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
			return mValue[0];
		}
		return defaultValue;
	}

} // end of QuakeEGLConfigChooser
