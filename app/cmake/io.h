#ifndef BROWSER_STDNS_H
#define BROWSER_STDNS_H

#include <iostream>
#include <cstdio>
#include <cstdlib>
#include "jni.h"

using namespace std;

namespace util {
    string toStdString(JNIEnv *env, jstring s, const char *charset) {
        if (s) {
            const jclass c = env->GetObjectClass(s);
            const jmethodID getBytes = env->GetMethodID(c, "getBytes", "(Ljava/lang/String;)[B");
            const jbyteArray stringBytes = (jbyteArray) env->CallObjectMethod(s, getBytes,
                                                                              env->NewStringUTF(
                                                                                      charset));
            size_t length = (size_t) env->GetArrayLength(stringBytes);
            jbyte *pByte = env->GetByteArrayElements(stringBytes, NULL);
            string r = string((char *) pByte, length);
            env->ReleaseByteArrayElements(stringBytes, pByte, JNI_ABORT);
            env->DeleteLocalRef(stringBytes);
            env->DeleteLocalRef(c);
            return r;
        } else return "";
    }
}

#endif