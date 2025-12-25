//
// Created by mihaelko27 on 12/24/25.
//

#ifndef BLOCKCHAIN_BLOCK_H
#define BLOCKCHAIN_BLOCK_H
#include <chrono>
#include <string>


class Block {
public:
    int index;
    std::string data;
    std::chrono::system_clock::time_point timestamp;
    std::string hash;
    std::string prevHash;
    long nonce;
    int difficulty;

    Block(int index, std::string data, std::string prevHash, int diff);

    void mineBlock();

    std::string calculateHash();

    bool isBlockValid(Block prevBlock, time_t currTime);

    std::string toString();

    std::string timeStampToString(const std::chrono::system_clock::time_point &time_point);
};


#endif //BLOCKCHAIN_BLOCK_H
