#include <jni.h>

#include <unistd.h>
#include <sys/reboot.h>

extern "C" jobject
Java_com_mindrex_standardassets_StandardContext_getDefaultContext(JNIEnv *env, __unused jclass clazz) {
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

void rebootLoader () {
    sync();
    reboot(RB_AUTOBOOT);
}