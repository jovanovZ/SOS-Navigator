//
// Created by mihaelko27 on 12/25/25.
//

#include "BlockChain.h"

#include <iostream>

BlockChain::BlockChain() {
  chain = std::vector<Block>();
  chain.push_back(Block(0, "Genesis block", "0", 5));
}

bool BlockChain::addBlock(Block newBlock) {
  Block prevBlock = chain.back();

  if (newBlock.isBlockValid(prevBlock, std::chrono::system_clock::to_time_t(
                                           std::chrono::system_clock::now()))) {
    chain.push_back(newBlock);
    return true;
  }

  std::cout << "Block not valid" << std::endl;
  return false;
}

bool BlockChain::isChainValid() const {
  const auto now =
      std::chrono::system_clock::to_time_t(std::chrono::system_clock::now());
  for (int i = 1; i < chain.size(); i++) {
    Block currBlock = chain[i];
    Block prevBlock = chain[i - 1];
    if (!currBlock.isBlockValid(prevBlock, now)) {
      return false;
    }
  }
  return true;
}

int BlockChain::getDifficulty() const {
  if (chain.size() % difficultyAdjustmentInterval != 0) {
    return chain.back().difficulty;
  }

  const Block &prevAdjustmentBlock =
      chain[chain.size() - difficultyAdjustmentInterval];
  const Block &lastBlock = chain.back();

  const double timeExpected =
      blockGenerationInterval * difficultyAdjustmentInterval;
  const double timeTaken =
      std::chrono::duration<double>(lastBlock.timestamp -
                                    prevAdjustmentBlock.timestamp)
          .count();

  int currentDifficulty = lastBlock.difficulty;
  int newDiff;

  if (timeTaken < timeExpected / 2.0) {
    newDiff = currentDifficulty + 1;
    std::cout << "Difficulty is increasing from: " << currentDifficulty
              << " to: " << newDiff << std::endl;
  } else if (timeTaken > timeExpected * 2.0) {
    newDiff = currentDifficulty - 1;
    std::cout << "Difficulty is decreasing from: " << currentDifficulty
              << " to: " << newDiff << std::endl;
  } else {
    newDiff = currentDifficulty;
    std::cout << "Difficulty stayed at " << newDiff << std::endl;
  }

  if (newDiff < 1) {
    newDiff = 1;
  }

  return newDiff;
}

std::string BlockChain::toString() const {
  std::string s = "";
  for (Block block : chain) {
    s += block.toString();
  }
  return s;
}

double BlockChain::getCumulativeDifficulty() const {
  double cumulativeDiff = 0;
  for (const Block block : chain) {
    cumulativeDiff += block.difficulty * block.difficulty;
  }
  return cumulativeDiff;
}
