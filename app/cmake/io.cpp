#include "io.h"
#include "socket.h"
#include <fstream>
#include <filesystem>

namespace fs = std::__fs::filesystem;

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
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
}
jstring
Java_com_mindrex_io_NativeFileSystem_ls(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        string a;
        for (const auto &entry : fs::directory_iterator(
                env->GetStringUTFChars(s, (jboolean *) false)))
            a += "\n" + entry.path().string();
        return env->NewStringUTF(a.c_str());
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
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
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
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
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
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
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
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
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
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
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
}
void
Java_com_mindrex_io_NativeFileSystem_createFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    const string a = env->GetStringUTFChars(s, (jboolean *) false);
    try {
        if (fs::exists(a))
            throw runtime_error("Target file already exists");
        else ofstream(a).close();
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
}
void
Java_com_mindrex_io_NativeFileSystem_createSocketFile(JNIEnv *env, __unused jobject clazz,
                                                      jstring s) {
    const string a = env->GetStringUTFChars(s, (jboolean *) false);
    try {
        skt::mkSkt(a.c_str());
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
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
        return (jint) p;
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
    return -1;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isSymlink(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_symlink(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
    return false;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isDirectory(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_directory(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
    return false;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_regular_file(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
    return false;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isBlockFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_block_file(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
    return false;
}

jboolean
Java_com_mindrex_io_NativeFileSystem_isSocketFile(JNIEnv *env, __unused jobject clazz, jstring s) {
    try {
        return fs::is_socket(env->GetStringUTFChars(s, (jboolean *) false));
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
    return false;
}
}