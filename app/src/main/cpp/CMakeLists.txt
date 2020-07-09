cmake_minimum_required(VERSION 3.4.1)

# opencv 头文件
include_directories(include)
# opencv 库文件
set(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")

# seetaface
add_subdirectory(FaceAlignment)
include_directories(FaceAlignment/include)

add_library( # Sets the name of the library.
        native-lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        native-lib.cpp
        FaceTrack.cpp
        )

target_link_libraries( # Specifies the target library.
        native-lib
        # Links the target library to the log library
        # included in the NDK.
        opencv_java4
        seeta_fa_lib
        log
        )