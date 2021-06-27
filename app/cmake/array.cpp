#include <jni.h>
#include <vector>

static std::vector<jobject> data;

extern "C" {
#pragma clang diagnostic push
#pragma ide diagnostic ignored "bugprone-reserved-identifier"
jlong Java_com_mindrex_data_ArrayHolder_init__Ljava_lang_Class_2I(JNIEnv *env, __unused jobject obj,
                                                                  jclass a, int b) {
    const jlong c = data.size();
    jobjectArray d = env->NewObjectArray(b, a, nullptr);
    data.push_back(env->NewGlobalRef(d));
    return c;
}

jlong
Java_com_mindrex_data_ArrayHolder_init___3Ljava_lang_Object_2(JNIEnv *env, __unused jobject obj,
                                                              jobjectArray a) {

}
#pragma clang diagnostic pop

jobject Java_com_mindrex_data_ArrayHolder_get(JNIEnv *env, jobject obj, jint i) {
    jclass _ = env->GetObjectClass(obj);
    jfieldID a = env->GetFieldID(_, "id", "J");
    const jlong b = env->GetLongField(obj, a);
    auto c = (jobjectArray) data.at(b);
    return env->GetObjectArrayElement(c, i);
}

void Java_com_mindrex_data_ArrayHolder_set(JNIEnv *env, jobject obj, jint i, jobject v) {
    jclass _ = (*env).GetObjectClass(obj);
    jfieldID x = (*env).GetFieldID(_, "id", "J");
    const jlong a = env->GetLongField(obj, x);
    auto b = (jobjectArray) data.at(a);
    env->SetObjectArrayElement(b, i, v);
}

jobject Java_com_mindrex_data_ArrayHolder_clone(JNIEnv *env, jobject obj) {
    jclass a = env->GetObjectClass(obj);
    jfieldID s = env->GetFieldID(a, "elementType", "Ljava/lang/Class;");
    auto b = (jclass) env->GetObjectField(obj, s);
    s = env->GetFieldID(a, "length", "I");
    const jint c = env->GetIntField(obj, s);
    jmethodID x = env->GetMethodID(a, "<init>", "(Ljava/lang/Class;I)V");
    jobject d = env->NewObject(a, x, b, c);
    s = env->GetFieldID(a, "id", "J");
    const jlong e = env->GetLongField(d, s);
    s = env->GetFieldID(a, "id", "J");
    const jlong f = env->GetLongField(obj, s);
    data.at(e) = data.at(f);
    return d;
}

#pragma clang diagnostic push
#pragma ide diagnostic ignored "bugprone-reserved-identifier"
jobjectArray Java_com_mindrex_data_ArrayHolder_toArray__Ljava_lang_Class_2(JNIEnv *env, jobject obj,
                                                                           jclass clazz) {
    jclass _ = env->GetObjectClass(obj);
    jfieldID s = env->GetFieldID(_, "id", "J");
    const jlong a = env->GetLongField(obj, s);
    auto b = (jobjectArray) data.at(a);
    const int c = env->GetArrayLength(b);
    jobjectArray d = env->NewObjectArray(c, clazz, nullptr);
    for (int i = 0; i < c; i++) {
        jobject e = env->GetObjectArrayElement(b, i);
        env->SetObjectArrayElement(d, i, e);
    }
    return d;
}

jobject Java_com_mindrex_data_ArrayHolder_toArray__(JNIEnv *env, jobject obj) {
    jclass _ = env->GetObjectClass(obj);
    jfieldID a = env->GetFieldID(_, "id", "J");
    const jlong b = env->GetLongField(obj, a);
    return data.at(b);
}
#pragma clang diagnostic pop

void Java_com_mindrex_data_ArrayHolder_finalize(JNIEnv *env, jobject obj) {
    jclass _ = env->GetObjectClass(obj);
    jfieldID a = env->GetFieldID(_, "id", "J");
    const jlong b = env->GetLongField(obj, a);
    data.at(b) = nullptr; // release memory
}
}