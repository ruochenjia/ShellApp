#ifndef BROWSER_STDOBJS_H
#define BROWSER_STDOBJS_H

typedef bool boolean;
typedef signed char byte;
typedef unsigned char ubyte;
typedef unsigned short ushort;
typedef unsigned int uint;
typedef unsigned long ulong;
typedef const char *string;

static const int null = 0;

namespace std {
    typedef const boolean boolean;
    typedef const byte byte;
    typedef const ubyte ubyte;
    typedef const ushort ushort;
    typedef const uint uint;
    typedef const ulong ulong;
}

#endif