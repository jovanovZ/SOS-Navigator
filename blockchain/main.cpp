#include "BlockChain.h"
#include <iostream>
#include <omp.h>
#include <string>

int main(int argc, char *argv[]) {
  BlockChain bc;

  bool useMPI = false;
  unsigned int threadCount = 0;
  if (argc < 3) {
    std::cerr << "Usage: " << argv[0] << " -t <thread_count> [-mpi]"
              << std::endl;
    return 1;
  }
  std::string arg1(argv[1]);
  if (arg1 != "-t") {
    std::cerr << "Error: first argument must be -t <thread_count>" << std::endl;
    return 1;
  }

  try {
    threadCount = std::stoul(argv[2]);
  } catch (...) {
    std::cerr << "Error: invalid thread count" << std::endl;
    return 1;
  }

  if (threadCount == 0) {
    std::cerr << "Error: thread count must be > 0" << std::endl;
    return 1;
  }

  if (argc == 4) {
    std::string arg3(argv[3]);
    if (arg3 == "-mpi") {
      useMPI = true;
    } else {
      std::cerr << "Error: unknown optional argument: " << arg3 << std::endl;
      return 1;
    }
  } else if (argc > 4) {
    std::cerr << "Error: too many arguments" << std::endl;
    return 1;
  }

  omp_set_num_threads(threadCount);
  const int BLOCK_COUNT = 50;

  if (useMPI) {

  } else {

    auto start = std::chrono::high_resolution_clock::now();
    for (int i = 1; i <= BLOCK_COUNT; i++) {
      const Block &prevBlock = bc.getChain().back();
      //      int diff = bc.getDifficulty();
      int diff = prevBlock.difficulty;
      Block newBlock(i, "test" + std::to_string(i), prevBlock.hash, diff);
      newBlock.mineBlock();
      // dodamo blok v verigo
      bc.addBlock(newBlock);

      std::cout << "Mined block " << i << " | Hash: " << newBlock.hash
                << " | Nonce: " << newBlock.nonce << " | Difficulty: " << diff
                << std::endl;
    }

    auto end = std::chrono::high_resolution_clock::now();
    auto durationMs =
        std::chrono::duration_cast<std::chrono::milliseconds>(end - start);

    std::cout << "Time to mine " << BLOCK_COUNT << " on " << threadCount
              << " threads" << std::endl;
    std::cout << durationMs.count() << "ms" << std::endl;
  }
  return 0;
}
