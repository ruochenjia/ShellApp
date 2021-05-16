#include <jni.h>
#include <iostream>

extern "C" {
jobject
Java_com_mindrex_android_appsupport_StandardContext_getDefaultContext(JNIEnv *env,
                                                                  __unused jclass clazz) {
    jclass thread = (*env).FindClass("android/app/ActivityThread");
    jmethodID currentThread = (*env).GetStaticMethodID(thread,
                                                       "currentActivityThread",
                                                       "()Landroid/app/ActivityThread;");
    jobject a = (*env).CallStaticObjectMethod(thread, currentThread);
    jmethodID getApplication = (*env).GetMethodID(thread, "getApplication",
                                                  "()Landroid/app/Application;");
    jobject context = (*env).CallObjectMethod(a, getApplication);
    return context;
}

jobjectArray
Java_com_mindrex_util_Reflection_newArray(JNIEnv *env, __unused jclass clazz, jclass a, jint b) {
    return env->NewObjectArray(b, a, nullptr);
}
}