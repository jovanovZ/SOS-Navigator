//
// Created by mihaelko27 on 12/25/25.
//

#ifndef BLOCKCHAIN_BLOCKCHAIN_H
#define BLOCKCHAIN_BLOCKCHAIN_H
#include <vector>

#include "Block.h"

class BlockChain {
  const int blockGenerationInterval = 10; // v sekundah
  const int difficultyAdjustmentInterval =
      10; // na koliko blokov se bo posodobil diff
public:
  std::vector<Block> chain;
  BlockChain();
  bool addBlock(Block newBlock);
  bool isChainValid() const;
  int getDifficulty() const;
  double getCumulativeDifficulty() const;
  std::string toString() const;

  const std::vector<Block> &getChain() const { return chain; }
};

#endif // BLOCKCHAIN_BLOCKCHAIN_H
