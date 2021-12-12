cmake_minimum_required(VERSION 3.4.1)
message(STATUS ABI_INFO  = ${ANDROID_ABI})

add_library( monerujo
        SHARED
        src/main/cpp/monerujo.cpp )

set(EXTERNAL_LIBS_DIR ${CMAKE_SOURCE_DIR}/../external-libs)

############
# libsqlite
############
add_library(libmerged STATIC IMPORTED)
set_target_properties(libmerged PROPERTIES IMPORTED_LOCATION
        ${EXTERNAL_LIBS_DIR}/${ANDROID_ABI}/lib/libwallet_merged.a)


#############
# System
#############

find_library( log-lib log )

include_directories( ${EXTERNAL_LIBS_DIR}/${ANDROID_ABI}/include )

message(STATUS EXTERNAL_LIBS_DIR :  ${EXTERNAL_LIBS_DIR})

#if(${ANDROID_ABI} STREQUAL "x86_64")
#    set(EXTRA_LIBS "wallet-crypto")
#else()
#    set(EXTRA_LIBS "")
#endif()

target_link_libraries(  monerujo
        libmerged
        ${log-lib}
        )