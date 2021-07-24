cmake_minimum_required(VERSION 3.18.1)
project("cmaketest")
add_library(native-lib SHARED
        android.cpp
        android.h
        array.cpp
        context.cpp
        io.cpp
        socket.h
        stdobjs.h
        )
find_library(log-lib log)
target_link_libraries(native-lib ${log-lib})