#add .a https://blog.csdn.net/yoyo_newbie/article/details/74427938
cmake_minimum_required (VERSION 2.8)
project(cpplibrary)

set(CMAKE_C_FLAGS_DEBUG "-g -Wall")
set(CMAKE_CXX_FLAGS_DEBUG ${CMAKE_C_FLAGS_DEBUG})
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wno-deprecated-declarations -Wno-reorder")
if(CLANG)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()

message("tjy")
set(SRCPATH ${CMAKE_CURRENT_SOURCE_DIR})

message("${CMAKE_CURRENT_SOURCE_DIR}")
message("${SRCPATH}")

include_directories("${SRCPATH}/libyuvWrap/include")
include_directories("${SRCPATH}/libyuvWrap")
include_directories("${SRCPATH}/libyuvJNI")
set(TARGET_NAME ${PROJECT_NAME})
message("target name ${TARGET_NAME}")
set (PUBLIC_YUV_LIBS ${SRCPATH}/libyuvWrap/${ANDROID_ABI}/libyuv.a)
message("yuv dir ${PUBLIC_YUV_LIBS}")
add_library(yuvlib STATIC IMPORTED)
set_target_properties(yuvlib
  PROPERTIES IMPORTED_LOCATION
  ${PUBLIC_YUV_LIBS})



message("heiheihei")
set(SRC_PATH ${SRCPATH})
message("SRC_PATH ${SRC_PATH}")
set(COMMON_PATH ${SRC_PATH}/libyuvWrap)
message("COMMON_PATH ${COMMON_PATH}")
set(SOURCES
	${COMMON_PATH}/libyuvWrap.cpp
	${SRC_PATH}/libyuvJNI/libyuvJni.cpp
)


set (PUBLIC_LINK_LIBS dl log android yuvlib)

add_library (${TARGET_NAME} SHARED ${SOURCES})
target_link_libraries(${TARGET_NAME} ${PUBLIC_LINK_LIBS})