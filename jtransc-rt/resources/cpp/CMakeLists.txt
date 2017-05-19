cmake_minimum_required(VERSION 2.8.9)
project (program)
{% for folder in CPP_INCLUDE_FOLDERS %}
include_directories({{ folder|escape }})
{% end %}
{% for folder in CPP_LIB_FOLDERS %}
link_directories({{ folder|escape }})
{% end %}

add_definitions(-DGC_NOT_DLL=1)

add_executable(program program.cpp)

#add_compile_options(-std=c++11)

set (CMAKE_CXX_STANDARD 11)

target_link_libraries(program gc-lib)
