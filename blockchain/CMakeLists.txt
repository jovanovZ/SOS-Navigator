cmake_minimum_required(VERSION 3.30)
project(blockchain)

set(CMAKE_CXX_STANDARD 20)

find_package(OpenSSL REQUIRED)

add_executable(blockchain
        main.cpp
        Block.cpp
        Block.h
)

target_link_libraries(blockchain OpenSSL::SSL OpenSSL::Crypto)
