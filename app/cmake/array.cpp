#include <jni.h>
#include <vector>

static std::vector<jobjectArray> data;

extern "C" {
jlong Java_com_mindrex_data_ArrayHolder_init(JNIEnv *env, __unused jobject obj, jclass a, int b) {
    const jlong c = data.size();
    jobjectArray d = env->NewObjectArray(b, a, nullptr);
    data.push_back(d);
    return c;
}

jobject Java_com_mindrex_data_ArrayHolder_get(JNIEnv *env, jobject obj, jint i) {
    const jlong a = env->GetLongField(obj, env->GetFieldID(env->GetObjectClass(obj), "id", "J"));
    jobjectArray b = data.at(a);
    return env->GetObjectArrayElement(b, i);
}

void Java_com_mindrex_data_ArrayHolder_set(JNIEnv *env, jobject obj, jint i, jobject v) {
    const jlong a = env->GetLongField(obj, env->GetFieldID(env->GetObjectClass(obj), "id", "J"));
    jobjectArray b = data.at(a);
    env->SetObjectArrayElement(b, i, v);
}

jobject Java_com_mindrex_data_ArrayHolder_clone(JNIEnv *env, jobject obj) {
    jclass a = env->GetObjectClass(obj);
    auto b = (jclass) env->GetObjectField(obj,
                                          env->GetFieldID(a, "elementType", "Ljava/lang/Class;"));
    const jint c = env->GetIntField(obj, env->GetFieldID(a, "length", "I"));
    jobject d = env->NewObject(a, env->GetMethodID(a, "<init>", "(Ljava/lang/Class;I)V"), b, c);
    const jlong e = env->GetLongField(d, env->GetFieldID(a, "id", "J"));
    const jlong f = env->GetLongField(obj, env->GetFieldID(a, "id", "J"));
    data.at(e) = data.at(f);
    return d;
}
}