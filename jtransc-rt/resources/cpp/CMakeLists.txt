cmake_minimum_required(VERSION 3.1)

#if(NOT CMAKE_BUILD_TYPE)
#  set(CMAKE_BUILD_TYPE Release)
#endif()

set (CMAKE_CXX_STANDARD 11)

#set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/archive")
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib")
#set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin")

set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}")

{% for item in CMAKE %}
{{ item }}
{% end %}

project (program)

{% for item in CMAKE_PROJECT %}
{{ item }}
{% end %}

{% for folder in CPP_INCLUDE_FOLDERS %}
include_directories({{ folder|escape }})
{% end %}

{% for folder in CPP_LIB_FOLDERS %}
link_directories({{ folder|escape }})
{% end %}

{% for define in CPP_DEFINES %}
add_definitions(-D{{ define }})
{% end %}

IF ("${CMAKE_CXX_COMPILER_ID}" STREQUAL "MSVC")
	add_compile_options(/wd4244)
ELSE()
	add_compile_options(-Wno-parentheses-equality)
	add_compile_options(-Wno-unused-value)
ENDIF()

add_executable(program program.cpp)

#if(UNIX)
#	set(CMAKE_CXX_FLAGS "-Wall -Wextra -Wno-unused-parameter -Wno-unused-variable")
#	set(CMAKE_CXX_FLAGS_DEBUG "-g")
#	set(CMAKE_CXX_FLAGS_RELEASE "-O3")
#
#	if("${CMAKE_BUILD_TYPE}" STREQUAL "Release")
#		#message("Release mode: strip")
#		add_custom_command(TARGET program POST_BUILD COMMAND strip ${CMAKE_CURRENT_SOURCE_DIR}/program)
#	endif()
#endif()

if(UNIX AND NOT APPLE)
	set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -pthread")
endif()

if(UNIX AND NOT WIN32)
	target_link_libraries(program dl)
endif()

{% for lib in CPP_LIBS %}
target_link_libraries(program {{ lib }})
{% end %}