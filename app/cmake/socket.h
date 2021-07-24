#ifndef BROWSER_SOCKET_H
#define BROWSER_SOCKET_H

#include <fcntl.h>
#include <sys/un.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

namespace skt {
    inline void createSocket(const char *path) {
        struct sockaddr_un a{};
        a.sun_family = AF_UNIX;
        strncpy(a.sun_path, path, UNIX_PATH_MAX);
        const int b = socket(AF_UNIX, SOCK_DGRAM, 0);
        bind(b, (struct sockaddr*) &a, sizeof(struct sockaddr_un));
        close(b);
    }
}

#endif