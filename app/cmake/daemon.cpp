


#include <cstdlib>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "socket.h"

static void skeleton_daemon() {
    pid_t pid = fork();
    if (pid < 0)
        exit(EXIT_FAILURE);
    if (pid > 0)
        exit(EXIT_SUCCESS);
    if (setsid() < 0)
        exit(EXIT_FAILURE);
    signal(SIGCHLD, SIG_IGN);
    signal(SIGHUP, SIG_IGN);
    pid = fork();
    if (pid < 0)
        exit(EXIT_FAILURE);
    if (pid > 0)
        exit(EXIT_SUCCESS);
    umask(0);
    chdir("/");
    for (int x = sysconf(_SC_OPEN_MAX); x >= 0; x--) {
        close(x);
    }
}

int start(char *path) {
    skeleton_daemon();
    while (true) {
        skt::mkSkt(path);
        sleep(20);
        break;
    }
    return EXIT_SUCCESS;
}

int main(int a, char **b) {
    if (a >= 1)
        return start(b[0]);
    else throw "Invalid arguments";
}

#include <jni.h>

extern "C" int Java_shell_app_Application_startDaemon(JNIEnv *env, jclass clazz, jstring a) {
    return start((char *) env->GetStringUTFChars(a, (jboolean *) false));
}