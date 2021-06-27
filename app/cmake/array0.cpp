#include <jni.h>

extern "C" {
jint Java_com_mindrex_data_Array_length(JNIEnv *env, __unused jclass clazz, jobject obj) {
    return env->GetArrayLength((jarray) obj);
}

jobject Java_com_mindrex_data_Array_get(JNIEnv *env, __unused jclass clazz, jobject obj, jint a) {
    return env->GetObjectArrayElement((jobjectArray) obj, a);
}
}