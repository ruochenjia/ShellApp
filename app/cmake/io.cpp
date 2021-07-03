#include <jni.h>
#include "socket.h"
#include <fstream>
#include <utime.h>
#include <vector>
#include <filesystem>

namespace fs = std::__fs::filesystem;

using namespace std;

extern "C" {
void
Java_com_mindrex_io_NativeFileSystem_copyFile(JNIEnv *env, __unused jobject clazz, jstring a,
                                              jstring b) {
    const string a1 = env->GetStringUTFChars(a, (jboolean *) false);
    const string b1 = env->GetStringUTFChars(b, (jboolean *) false);
    try {
        if (fs::exists(b1))
            throw runtime_error("Destination file already exists");
        else if (fs::exists(a1)) {
            if (fs::is_directory(a1))
                fs::copy(a1, b1);
            else if (fs::is_symlink(a1))
                fs::copy_symlink(a1, b1);
            else fs::copy_file(a1, b1);
        } else throw runtime_error("Source file does not exist");
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}
jobjectArray
Java_com_mindrex_io_NativeFileSystem_listChildren(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        std::vector<string> a;
        for (const auto &b : fs::directory_iterator(
                env->GetStringUTFChars(s, (jboolean *) false))) {
            a.push_back(b.path().string());
        }
        jobjectArray b = env->NewObjectArray(a.size(), env->FindClass("java/lang/String"),
                                             nullptr);
        for (int i = 0; i < a.size(); i++)
            env->SetObjectArrayElement(b, i, env->NewStringUTF(a.at(i).c_str()));
        return b;
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return nullptr;
}
void
Java_com_mindrex_io_NativeFileSystem_deleteFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    const string a = env->GetStringUTFChars(s, (jboolean *) false);
    try {
        if (fs::exists(a)) {
            if (fs::is_directory(a)) {
                fs::remove_all(a);
            } else if (fs::is_regular_file(a) || fs::is_symlink(a)) {
                fs::remove(a);
            } else throw runtime_error("Unsupported file type");
        } else throw runtime_error("Target file does not exist");
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}
void
Java_com_mindrex_io_NativeFileSystem_renameFile(JNIEnv *env, __unused jobject clazz, jstring a,
                                                jstring b) {
    const string a1 = env->GetStringUTFChars(a, (jboolean *) false);
    const string b1 = env->GetStringUTFChars(b, (jboolean *) false);
    try {
        if (!fs::exists(a1))
            throw runtime_error("Source file does not exist");
        else if (fs::exists(b1))
            throw runtime_error("Destination file already exists");
        else fs::rename(a1, b1);
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}
void
Java_com_mindrex_io_NativeFileSystem_createSymlink(JNIEnv *env, __unused jobject clazz, jstring a,
                                                   jstring b) {
    const string a1 = env->GetStringUTFChars(a, (jboolean *) false);
    const string b1 = env->GetStringUTFChars(b, (jboolean *) false);
    try {
        if (!fs::exists(a1))
            throw runtime_error("Source file does not exist");
        else if (fs::exists(b1))
            throw runtime_error("Destination file already exists");
        else {
            if (fs::is_directory(a1))
                fs::create_directory_symlink(a1, b1);
            else fs::create_symlink(a1, b1);
        }
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}
void
Java_com_mindrex_io_NativeFileSystem_createHardLink(JNIEnv *env, __unused jobject clazz, jstring a,
                                                    jstring b) {
    const string a1 = env->GetStringUTFChars(a, (jboolean *) false);
    const string b1 = env->GetStringUTFChars(b, (jboolean *) false);
    try {
        if (!fs::exists(a1))
            throw runtime_error("Source file does not exist");
        else if (fs::exists(b1))
            throw runtime_error("Destination file already exists");
        else fs::create_hard_link(a1, b1);
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}
void
Java_com_mindrex_io_NativeFileSystem_createDirectory(JNIEnv *env, __unused jobject clazz,
                                                     jstring s) {
    const string a = env->GetStringUTFChars(s, (jboolean *) false);
    try {
        if (fs::exists(a))
            throw runtime_error("Target file already exists");
        else fs::create_directories(a);
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}
void
Java_com_mindrex_io_NativeFileSystem_createFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    const string a = env->GetStringUTFChars(s, (jboolean *) false);
    try {
        if (fs::exists(a))
            throw runtime_error("Target file already exists");
        else std::ofstream(a).close();
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}
void
Java_com_mindrex_io_NativeFileSystem_createSocketFile(JNIEnv *env, __unused jobject clazz,
                                                      jstring s) {
    try {
        skt::createSocket(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}
jboolean
Java_com_mindrex_io_NativeFileSystem_exists(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::exists(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        return false;
    }
}
jint
Java_com_mindrex_io_NativeFileSystem_getFilePermission(JNIEnv *env, __unused jobject clazz,
                                                       jstring s) {
    try {
        const fs::perms p = fs::status(env->GetStringUTFChars(s, (jboolean *) false)).permissions();
        return (int) p;
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return -1;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isSymlink(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_symlink(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return false;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isDirectory(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_directory(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return false;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_regular_file(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return false;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isBlockFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_block_file(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return false;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isSocketFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_socket(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return false;
}

jlong
Java_com_mindrex_io_NativeFileSystem_getFileSize(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::file_size(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return -1;
}

jlong Java_com_mindrex_io_NativeFileSystem_getFileLastModified(JNIEnv *env, __unused jobject clazz,
                                                               jstring s) {
    try {
//        const char *a = env->GetStringUTFChars(s, (jboolean *) false);
//        struct stat64 attr{};
//        stat64(a, &attr);
//        return attr.st_mtime;
        jclass a = env->FindClass("java/io/File");
        jobject b = env->NewObject(a, env->GetMethodID(a, "<init>", "(Ljava/lang/String;)V"), s);
        jobject c = env->GetStaticObjectField(a, env->GetStaticFieldID(a, "fs",
                                                                       "Ljava/io/FileSystem;"));
        return env->CallLongMethod(c, env->GetMethodID(env->FindClass("java/io/FileSystem"),
                                                       "getLastModifiedTime", "(Ljava/io/File;)J"),
                                   b);
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
    return (uint) -1l;
}

void Java_com_mindrex_io_NativeFileSystem_setFileLastModified(JNIEnv *env, __unused jobject clazz,
                                                              jstring s, jlong j) {
    jclass a = env->FindClass("java/io/File");
    jobject b = env->NewObject(a, env->GetMethodID(a, "<init>", "(Ljava/lang/String;)V"), s);
    jobject c = env->GetStaticObjectField(a,
                                          env->GetStaticFieldID(a, "fs", "Ljava/io/FileSystem;"));
    env->CallBooleanMethod(c, env->GetMethodID(env->FindClass("java/io/FileSystem"),
                                               "setLastModifiedTime", "(Ljava/io/File;J)Z"), b, j);
}

void Java_com_mindrex_io_NativeFileSystem_setFilePermission(JNIEnv *env, __unused jobject clazz,
                                                            jstring s, jint i) {
    try {
        fs::permissions(env->GetStringUTFChars(s, (jboolean *) false), (fs::perms) i,
                        fs::perm_options::replace);
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"), e.what());
    }
}

void
Java_com_mindrex_io_NativeFileSystem_setFileOwner(JNIEnv *env, __unused jobject clazz, jstring s,
                                                  jint a, jint b) {
    if (chown(env->GetStringUTFChars(s, (jboolean *) false), a, b) == -1)
        env->ThrowNew(env->FindClass("com/mindrex/gm/NativeInterfaceException"),
                      "Cannot run chmod");
}

jint
Java_com_mindrex_io_NativeFileSystem_getFileOwner(JNIEnv *env, __unused jobject clazz, jstring s) {
    const char *a = env->GetStringUTFChars(s, (jboolean *) false);
    struct stat attr{};
    stat(a, &attr);
    return attr.st_uid;
}

jint
Java_com_mindrex_io_NativeFileSystem_getFileGroup(JNIEnv *env, __unused jobject clazz, jstring s) {
    const char *a = env->GetStringUTFChars(s, (jboolean *) false);
    struct stat attr{};
    stat(a, &attr);
    return attr.st_gid;
}
}