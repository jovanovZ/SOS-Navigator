cmake_minimum_required(VERSION 3.30)
project(blockchain)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Poišči OpenSSL
find_package(OpenSSL REQUIRED)

# Omogoči OpenMP
find_package(OpenMP REQUIRED)

add_executable(blockchain
        main.cpp
        Block.cpp
        Block.h
        BlockChain.cpp
        BlockChain.h
)

# Linka OpenSSL in OpenMP
target_link_libraries(blockchain PRIVATE OpenSSL::SSL OpenSSL::Crypto OpenMP::OpenMP_CXX)

