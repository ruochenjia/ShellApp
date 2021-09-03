cmake_minimum_required(VERSION 3.18.1)
project("cmaketest")
find_package(JNI)
if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif()
add_library(native-lib SHARED
        array.cpp
        context.cpp
        io.cpp
        socket.h
        stdobjs.h
        )
find_library(log-lib log)
target_link_libraries(native-lib ${log-lib})
