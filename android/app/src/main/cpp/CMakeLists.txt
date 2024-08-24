cmake_minimum_required(VERSION 3.29.0)

project("passknight")

set(BORINGSSL_ROOT "${ANDROID_NDK}/sources/third_party/boringssl")

add_subdirectory(${BORINGSSL_ROOT} ${CMAKE_BINARY_DIR}/boringssl)

add_library(pkcryptonative SHARED pkcryptonative.cpp)

include_directories(${BORINGSSL_ROOT}/include)

target_link_libraries(pkcryptonative ssl crypto log)