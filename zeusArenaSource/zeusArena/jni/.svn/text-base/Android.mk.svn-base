# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.crg/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)



include $(CLEAR_VARS)

# from clear-vars.mk :
#NDK_LOCAL_VARS := \
#  LOCAL_MODULE \
#  LOCAL_SRC_FILES \
#  LOCAL_CFLAGS \
#  LOCAL_LDFLAGS \
#  LOCAL_ARFLAGS \
#  LOCAL_CPP_EXTENSION \
#  LOCAL_STATIC_LIBRARIES \
#  LOCAL_STATIC_WHOLE_LIBRARIES \
#  LOCAL_SHARED_LIBRARIES \
#  LOCAL_MAKEFILE \
#  LOCAL_NO_UNDEFINED_SYMBOLS \

LOCAL_MODULE    := quake2
LOCAL_SRC_FILES := src/android/quake2-jni.c

#TARGET_CPU_ABI   := armeabi-v7a


#gcc -Wall -pipe -Dstricmp=strcasecmp -O2 -ffast-math -funroll-loops -falign-loops=2 -falign-jumps=2 -falign-functions=2 -fno-strict-aliasing -DLINUX_VERSION='"3.21+r0.16"' -

LOCAL_CFLAGS :=  \
 -Dstricmp=strcasecmp -DREF_HARD_LINKED -DGAME_HARD_LINKED 
#LOCAL_CFLAGS += -I/media/partage/outils/mydroid/frameworks/base/opengl/include

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS += -DHAVE_NEON=1
#    LOCAL_SRC_FILES += helloneon-intrinsics.c.neon
    LOCAL_ARM_NEON := true

endif


   
#LOCAL_LDFLAGS := /media/partage/outils/mydroid/out/target/product/generic/system/lib/libGLESv1_CM.so
#LOCAL_LDFLAGS += /media/partage/outils/mydroid/out/target/product/generic/symbols/system/lib/libGLESv1_CM.so
#LOCAL_LDFLAGS += ./external/libGLESv1_CM.so
#LOCAL_LDFLAGS += ./build/platforms/android-4/arch-arm/usr/lib/libGLESv1_CM.so
LOCAL_LDLIBS += -lGLESv1_CM


#LOCAL_LDFLAGS += ./build/platforms/android-3/arch-arm/usr/lib/liblog.so
LOCAL_LDLIBS += -llog

#LOCAL_LDFLAGS += -export-symbols-regex Java_com_example_quake2
#LOCAL_LDFLAGS += -export-symbols-regex Java_com_example_quake2_Quake2_Quake2Test
#LOCAL_LDFLAGS += -fvisibility=hidden 
LOCAL_CFLAGS += -fvisibility=hidden 

#./out/target/product/generic/obj/lib/libGLESv1_CM.so
#./out/target/product/generic/system/lib/libGLESv1_CM.so
#./out/target/product/generic/symbols/system/lib/libGLESv1_CM.so

#  make APP=quake2 V=1

#Compile thumb  : quake2 <= sources/quake2/src/null/snddma_null.c

#build/prebuilt/linux-x86/arm-eabi-4.2.1/bin/arm-eabi-gcc -Ibuild/platforms/android-1.5/arch-arm/usr/include -march=armv5te -mtune=xscale -msoft-float -fpic -mthumb-interwork -ffunction-sections -funwind-tables -fstack-protector -fno-short-enums -D__ARM_ARCH_5__ -D__ARM_ARCH_5T__ -D__ARM_ARCH_5E__ -D__ARM_ARCH_5TE__   -Isources/quake2 -DANDROID  -O2 -DNDEBUG -g    -c -MMD -MP -MF out/apps/quake2/android-1.5-arm/objs/quake2/src/null/snddma_null.c.d.tmp sources/quake2/src/null/snddma_null.c -o out/apps/quake2/android-1.5-arm/objs/quake2/src/null/snddma_null.c

#build/core/mkdeps.sh out/apps/quake2/android-1.5-arm/objs/quake2/src/null/snddma_null.c out/apps/quake2/android-1.5-arm/objs/quake2/src/null/snddma_null.c.d.tmp out/apps/quake2/android-1.5-arm/objs/quake2/src/null/snddma_null.c.d

#SharedLibrary  : libquake2.so

#build/prebuilt/linux-x86/arm-eabi-4.2.1/bin/arm-eabi-gcc -nostdlib -Wl,-soname,libquake2.so -Wl,-shared,-Bsymbolic  out/apps/quake2/android-1.5-arm/objs/quake2/quake2-jni.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_ai.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/p_client.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_chase.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_cmds.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_svcmds.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_combat.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_func.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_items.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_main.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_misc.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_monster.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_phys.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_save.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_spawn.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_target.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_trigger.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_turret.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_utils.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/g_weapon.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_actor.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_berserk.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_boss2.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_boss3.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_boss31.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_boss32.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_brain.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_chick.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_flipper.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_float.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_flyer.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_gladiator.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_gunner.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_hover.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_infantry.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_insane.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_medic.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_move.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_mutant.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_parasite.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_soldier.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_supertank.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_tank.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/p_hud.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/p_trail.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/p_view.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/p_weapon.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/q_shared.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_flash.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_cin.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_ents.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_fx.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_input.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_inv.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_main.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_parse.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_pred.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_tent.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_scrn.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_view.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/cl_newfx.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/console.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/keys.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/menu.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/snd_dma.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/snd_mem.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/snd_mix.c out/apps/quake2/android-1.5-arm/objs/quake2/src/client/qmenu.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/m_flash.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/cmd.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/cmodel.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/common.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/crc.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/cvar.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/files.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/md4.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/net_chan.c out/apps/quake2/android-1.5-arm/objs/quake2/src/server/sv_ccmds.c out/apps/quake2/android-1.5-arm/objs/quake2/src/server/sv_ents.c out/apps/quake2/android-1.5-arm/objs/quake2/src/server/sv_game.c out/apps/quake2/android-1.5-arm/objs/quake2/src/server/sv_init.c out/apps/quake2/android-1.5-arm/objs/quake2/src/server/sv_main.c out/apps/quake2/android-1.5-arm/objs/quake2/src/server/sv_send.c out/apps/quake2/android-1.5-arm/objs/quake2/src/server/sv_user.c out/apps/quake2/android-1.5-arm/objs/quake2/src/server/sv_world.c out/apps/quake2/android-1.5-arm/objs/quake2/src/linux/q_shlinux.c out/apps/quake2/android-1.5-arm/objs/quake2/src/linux/vid_menu.c out/apps/quake2/android-1.5-arm/objs/quake2/src/linux/vid_so.c out/apps/quake2/android-1.5-arm/objs/quake2/src/android/sys_android.c out/apps/quake2/android-1.5-arm/objs/quake2/src/linux/glob.c out/apps/quake2/android-1.5-arm/objs/quake2/src/linux/net_udp.c out/apps/quake2/android-1.5-arm/objs/quake2/src/game/q_shared.c out/apps/quake2/android-1.5-arm/objs/quake2/src/qcommon/pmove.c out/apps/quake2/android-1.5-arm/objs/quake2/src/null/cd_null.c out/apps/quake2/android-1.5-arm/objs/quake2/src/null/snddma_null.c -Wl,--whole-archive  -Wl,--no-whole-archive   build/platforms/android-1.5/arch-arm/usr/lib/libc.so build/platforms/android-1.5/arch-arm/usr/lib/libstdc++.so build/platforms/android-1.5/arch-arm/usr/lib/libm.so   -Wl,--no-undefined   -Wl,-rpath-link=build/platforms/android-1.5/arch-arm/usr/lib /media/partage/outils/android-ndk-1.5_r1/build/prebuilt/linux-x86/arm-eabi-4.2.1/bin/../lib/gcc/arm-eabi/4.2.1/interwork/libgcc.a -o out/apps/quake2/android-1.5-arm/libquake2.so

#############################################################################
# CLIENT/SERVER
#############################################################################

QUAKE2_SRC = \
	src/client/cl_cin.c \
	src/client/cl_ents.c \
	src/client/cl_fx.c \
	src/client/cl_input.c \
	src/client/cl_inv.c \
	src/client/cl_main.c \
	src/client/cl_parse.c \
	src/client/cl_pred.c \
	src/client/cl_tent.c \
	src/client/cl_scrn.c \
	src/client/cl_view.c \
	src/client/cl_newfx.c \
	src/client/console.c \
	src/client/keys.c \
	src/client/menu.c \
	src/client/snd_dma.c \
	src/client/snd_mem.c \
	src/client/snd_mix.c \
	src/client/qmenu.c \
	\
	src/qcommon/cmd.c \
	src/qcommon/cmodel.c \
	src/qcommon/common.c \
	src/qcommon/crc.c \
	src/qcommon/cvar.c \
	src/qcommon/files.c \
	src/qcommon/md4.c \
	src/qcommon/net_chan.c \
	\
	src/server/sv_ccmds.c \
	src/server/sv_ents.c \
	src/server/sv_game.c \
	src/server/sv_init.c \
	src/server/sv_main.c \
	src/server/sv_send.c \
	src/server/sv_user.c \
	src/server/sv_world.c \
	\
	src/linux/q_shlinux.c \
	src/linux/vid_menu.c \
	src/android/vid_so.c \
	src/android/sys_android.c \
	src/linux/glob.c \
	src/linux/net_udp.c \
	\
	src/qcommon/pmove.c \
	\
	src/null/cd_null.c
	
#	src/null/snddma_null.c 
#	src/game/m_flash.c \
#	src/game/q_shared.c \

#############################################################################
# GAME
#############################################################################

GAME_SRC = \
	src/game/g_ai.c \
	src/game/p_client.c \
	src/game/g_chase.c \
	src/game/g_cmds.c \
	src/game/g_svcmds.c \
	src/game/g_combat.c \
	src/game/g_func.c \
	src/game/g_items.c \
	src/game/g_main.c \
	src/game/g_misc.c \
	src/game/g_monster.c \
	src/game/g_phys.c \
	src/game/g_save.c \
	src/game/g_spawn.c \
	src/game/g_target.c \
	src/game/g_trigger.c \
	src/game/g_turret.c \
	src/game/g_utils.c \
	src/game/g_weapon.c \
	src/game/m_actor.c \
	src/game/m_berserk.c \
	src/game/m_boss2.c \
	src/game/m_boss3.c \
	src/game/m_boss31.c \
	src/game/m_boss32.c \
	src/game/m_brain.c \
	src/game/m_chick.c \
	src/game/m_flipper.c \
	src/game/m_float.c \
	src/game/m_flyer.c \
	src/game/m_gladiator.c \
	src/game/m_gunner.c \
	src/game/m_hover.c \
	src/game/m_infantry.c \
	src/game/m_insane.c \
	src/game/m_medic.c \
	src/game/m_move.c \
	src/game/m_mutant.c \
	src/game/m_parasite.c \
	src/game/m_soldier.c \
	src/game/m_supertank.c \
	src/game/m_tank.c \
	src/game/p_hud.c \
	src/game/p_trail.c \
	src/game/p_view.c \
	src/game/p_weapon.c \
	src/game/q_shared.c \
	src/game/m_flash.c

#############################################################################
# REF_GL
#############################################################################

REF_GL_SRC = \
	src/ref_gl/gl_draw.c \
	src/ref_gl/gl_image.c \
	src/ref_gl/gl_light.c \
	src/ref_gl/gl_mesh.c \
	src/ref_gl/gl_model.c \
	src/ref_gl/gl_rmain.c \
	src/ref_gl/gl_rmisc.c \
	src/ref_gl/gl_rsurf.c \
	src/ref_gl/gl_warp.c \
	\
	src/android/rw_in_android.c \
	src/android/glimp_android.c \
	src/android/qgl_android.c \
	src/android/nanoWrap.c 

#	src/android/qgl_android.c \
#	src/linux/q_shlinux.c \
#	src/linux/glob.c \
# 	src/linux/qgl_linux.c \
#	src/game/q_shared.c \


	
LOCAL_SRC_FILES += $(GAME_SRC) 

LOCAL_SRC_FILES += $(QUAKE2_SRC) 

LOCAL_SRC_FILES += $(REF_GL_SRC) 

LOCAL_SRC_FILES += src/android/snd_android.c


include $(BUILD_SHARED_LIBRARY)








