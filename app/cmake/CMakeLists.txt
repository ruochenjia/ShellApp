cmake_minimum_required(VERSION 3.10.2)
project("cmaketest")
add_library(native-lib SHARED
        array.cpp
        array0.cpp
        context.cpp
        io.cpp
        socket.h
        testjni.h
        )
find_library(log-lib log)
target_link_libraries(native-lib ${log-lib})