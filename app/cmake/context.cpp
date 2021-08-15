#include <jni.h>
#include "stdobjs.h"

extern "C" {
jobject
Java_com_mindrex_android_appsupport_SupportContext_getDefaultContext(JNIEnv *env,
                                                                     __unused jclass clazz) {
    jclass a = env->FindClass("android/app/ActivityThread");
    jobject b = env->CallStaticObjectMethod(a, env->GetStaticMethodID(a, "currentActivityThread",
                                                                      "()Landroid/app/ActivityThread;"));
    return env->CallObjectMethod(b, env->GetMethodID(a, "getApplication",
                                                     "()Landroid/app/Application;"));
}

void Java_com_mindrex_gm_GmSystem_crs(__unused JNIEnv *env, __unused jclass clazz) {
    //env->ThrowNew(env->FindClass("jvm/runtime/InterruptedRuntimeError"), "\ue000");
    throw " n u l l ";
}

// reflection

jobjectArray
Java_com_mindrex_util_reflect_Reflection_newArray(JNIEnv *env, __unused jclass clazz, jclass a,
                                                  jint b) {
    return env->NewObjectArray(b, a, nullptr);
}
}