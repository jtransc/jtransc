cmake_minimum_required(VERSION 3.1)

set (CMAKE_CXX_STANDARD 11)
#add_compile_options(-std=c++11)
add_definitions(-DNO_GC=1)

project (program)

{% for folder in CPP_INCLUDE_FOLDERS %}
include_directories({{ folder|escape }})
{% end %}

{% for folder in CPP_LIB_FOLDERS %}
link_directories({{ folder|escape }})
{% end %}

{% for define in CPP_DEFINES %}
add_definitions(-D{{ define }})
{% end %}

add_executable(program program.cpp)

{% for lib in CPP_LIBS %}
target_link_libraries(program {{ lib }})
{% end %}