#ifndef BROWSER_TESTJNI_H
#define BROWSER_TESTJNI_H

typedef bool boolean;
typedef signed char byte;
typedef char *string;
struct _JNIEnv;
class object {};
class clazz : public object {};
class array : public object {};

typedef _JNIEnv JNIEnv;
typedef boolean jboolean;
typedef byte jbyte;
typedef unsigned char jchar;
typedef short jshort;
typedef int jint;
typedef long jlong;
typedef float jfloat;
typedef double jdouble;
typedef string jstring;
typedef object *jobject;
typedef clazz *jclass;
typedef array *jarray;
typedef jarray jobjectArray;

#include "../../../../AndroidSdk/ndk/23.0.7123448/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include/c++/v1/__config"

using namespace std;

#endif