cmake_minimum_required(VERSION 2.8.9)
project (program)
{% for folder in CPP_INCLUDE_FOLDERS %}
include_directories({{ folder|escape }})
{% end %}
add_executable(program program.cpp)