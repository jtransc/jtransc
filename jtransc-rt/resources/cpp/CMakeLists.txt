cmake_minimum_required(VERSION 2.8.9)
project (program)
{% for folder in CPP_INCLUDE_FOLDERS %}
include_directories({{ folder|escape }})
{% end %}
{% for folder in CPP_LIB_FOLDERS %}
link_directories({{ folder|escape }})
{% end %}
add_executable(program program.cpp)

target_link_libraries(program gcmt-dll)
