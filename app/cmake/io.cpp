#include "io.h"
#include "socket.h"
#include <fstream>
#include <filesystem>

namespace fs = std::__fs::filesystem;

extern "C" void
Java_com_mindrex_io_FileUtil_copy(JNIEnv *env, __unused jclass clazz, jstring a, jstring b) {
    const string a1 = util::toStdString(env, a, "UTF-8");
    const string b1 = util::toStdString(env, b, "UTF-8");
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

extern "C" void
Java_com_mindrex_io_FileUtil_delete(JNIEnv *env, __unused jclass clazz, jstring s) {
    const string a = util::toStdString(env, s, "UTF-8");
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

extern "C" void
Java_com_mindrex_io_FileUtil_rename(JNIEnv *env, __unused jclass clazz, jstring a, jstring b) {
    const string a1 = util::toStdString(env, a, "UTF-8");
    const string b1 = util::toStdString(env, b, "UTF-8");
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

extern "C" void
Java_com_mindrex_io_FileUtil_createSymlink(JNIEnv *env, __unused jclass clazz, jstring a,
                                           jstring b) {
    const string a1 = util::toStdString(env, a, "UTF-8");
    const string b1 = util::toStdString(env, b, "UTF-8");
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

extern "C" void
Java_com_mindrex_io_FileUtil_createHardLink(JNIEnv *env, __unused jclass clazz, jstring a,
                                            jstring b) {
    const string a1 = util::toStdString(env, a, "UTF-8");
    const string b1 = util::toStdString(env, b, "UTF-8");
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

extern "C" void
Java_com_mindrex_io_FileUtil_mkdir(JNIEnv *env, __unused jclass clazz, jstring s) {
    const string a = util::toStdString(env, s, "UTF-8");
    try {
        if (fs::exists(a))
            throw runtime_error("Target file already exists");
        else fs::create_directories(a);
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
}

extern "C" void
Java_com_mindrex_io_FileUtil_createNewFile(JNIEnv *env, __unused jclass clazz, jstring s) {
    const string a = util::toStdString(env, s, "UTF-8");
    try {
        if (fs::exists(a))
            throw runtime_error("Target file already exists");
        else ofstream(a).close();
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
}

extern "C" void
Java_com_mindrex_io_FileUtil_createSocketFile(JNIEnv *env, __unused jclass clazz, jstring s) {
    const string a = util::toStdString(env, s, "UTF-8");
    try {
        skt::mkSkt(a.c_str());
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
}

extern "C" jboolean
Java_com_mindrex_io_FileUtil_exists(JNIEnv *env, __unused jclass clazz, jstring s) {
    try {
        return fs::exists(util::toStdString(env, s, "UTF-8"));
    } catch (const exception &e) {
        return false;
    }
}

extern "C" jstring
Java_com_mindrex_io_FileUtil_getPermissionString(JNIEnv *env, __unused jclass clazz, jstring s) {
    try {
        const fs::perms p = fs::status(util::toStdString(env, s, "UTF-8")).permissions();
        const string r = string((p & fs::perms::owner_read) != fs::perms::none ? "r" : "-") +
                         ((p & fs::perms::owner_write) != fs::perms::none ? "w" : "-") +
                         ((p & fs::perms::owner_exec) != fs::perms::none ? "x" : "-") +
                         ((p & fs::perms::group_read) != fs::perms::none ? "r" : "-") +
                         ((p & fs::perms::group_write) != fs::perms::none ? "w" : "-") +
                         ((p & fs::perms::group_exec) != fs::perms::none ? "x" : "-") +
                         ((p & fs::perms::others_read) != fs::perms::none ? "r" : "-") +
                         ((p & fs::perms::others_write) != fs::perms::none ? "w" : "-") +
                         ((p & fs::perms::others_exec) != fs::perms::none ? "x" : "-");
        return env->NewStringUTF(r.c_str());
    } catch (const exception &e) {
        env->ThrowNew(env->FindClass("java/io/IOException"), e.what());
    }
}

extern "C" jbyte
Java_com_mindrex_io_FileUtil_fileType(JNIEnv *env, __unused jclass clazz, jstring s) {
    const string a = util::toStdString(env, s, "UTF-8");
    try {
        if (fs::is_regular_file(a)) return 0;
        else if (fs::is_directory(a)) return 1;
        else if (fs::is_symlink(a)) return 2;
        else if (fs::is_block_file(a)) return 3;
        else if (fs::is_socket(a)) return 4;
        else return -1;
    } catch (const exception &e) {
        return -128;
    }
}