cmake_minimum_required(VERSION 3.22.1)

project("orbital")

# 1. Build your native engine library from the C++ source file
add_library(${CMAKE_PROJECT_NAME} SHARED
        engine.cpp)

# 2. Find the pre-compiled Oboe package that Gradle downloaded
find_package(oboe REQUIRED CONFIG)

# 3. Link Oboe and Android's native logging to your C++ engine
target_link_libraries(${CMAKE_PROJECT_NAME}
        oboe::oboe
        log)