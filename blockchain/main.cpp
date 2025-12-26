#include <iostream>
#include "BlockChain.h"

int main() {
    BlockChain bc;

    std::cout << "=== Genesis block ===\n";
    std::cout << bc.toString() << std::endl;

    const int NUM_BLOCKS = 5;

    for (int i = 1; i <= NUM_BLOCKS; ++i) {
        int difficulty = bc.getDifficulty();

        const Block& prevBlock = bc.getChain().back();

        Block newBlock(
            i,
            "Data for block " + std::to_string(i),
            prevBlock.hash,
            difficulty
        );

        std::cout << "Mining block " << i
                  << " (difficulty " << difficulty << ")...\n";

        newBlock.mineBlock();

        if (bc.addBlock(newBlock)) {
            std::cout << "Block " << i << " added successfully\n\n";
        } else {
            std::cout << "Block " << i << " rejected\n\n";
        }
    }

    std::cout << "=== Final blockchain ===\n";
    std::cout << bc.toString() << std::endl;

    std::cout << "Chain valid: "
              << (bc.isChainValid() ? "YES" : "NO") << std::endl;

    std::cout << "Cumulative difficulty: "
              << bc.getCumulativeDifficulty() << std::endl;

    return 0;
}
