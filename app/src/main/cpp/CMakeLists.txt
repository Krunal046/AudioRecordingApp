cmake_minimum_required(VERSION 3.4.1)

add_library(
        native-recorder
        SHARED
        audio_recorder.cpp
)

find_library(log-lib        log)
find_library(opensles-lib   OpenSLES)

target_link_libraries(
        native-recorder
        ${log-lib}
        ${opensles-lib}
)
