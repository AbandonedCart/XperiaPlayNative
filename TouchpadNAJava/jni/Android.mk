
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := touchpadndkjava
LOCAL_SRC_FILES := Touchpad.c
LOCAL_LDLIBS    := -llog -landroid -lEGL -lGLESv1_CM

include $(BUILD_SHARED_LIBRARY)
