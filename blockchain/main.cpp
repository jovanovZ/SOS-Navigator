#include <iostream>

#include "Block.h"

int main() {
    std::time_t now = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now());

    Block genesisBlock(0, "Genesis Block", "0", 2);

    std::cout << "=== Genesis Block ! ===\n";
    std::cout << genesisBlock.toString() << "\n";

    bool valid = genesisBlock.isBlockValid(genesisBlock, now);
    std::cout << "Is genesis block valid? " << (valid ? "Yes" : "No") << "\n";

    std::cout << "Mining genesis block..." << std::endl;
    genesisBlock.mineBlock();

    std::cout << "Genesis Block after mining:\n";
    std::cout << genesisBlock.toString() << "\n";
    return 0;
}
