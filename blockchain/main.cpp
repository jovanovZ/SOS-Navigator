#include "BlockChain.h"
#include <cstring>
#include <iostream>
#include <mpi.h>
#include <omp.h>
#include <string>
#include <vector>

const int SERVER_RANK = 0;

const int TAG_TASK_HASH = 1;
const int TAG_TASK_DIFF = 2;
const int TAG_TASK_INDEX = 3;
const int TAG_RESULT_NONCE = 4;
const int TAG_STOP = 5;

void server(int num_of_processes, unsigned int blocksToMine) {
  BlockChain bc;
  auto start = std::chrono::high_resolution_clock::now();
  const int num_clients = num_of_processes - 1;

  for (int i = 1; i <= blocksToMine; i++) {
    const Block &prevBlock = bc.getChain().back();
    int diff = bc.getDifficulty();
    // int diff = prevBlock.difficulty;

    char prevHash_cstr[65];
    strncpy(prevHash_cstr, prevBlock.hash.c_str(), 65);
    MPI_Bcast(prevHash_cstr, 65, MPI_CHAR, SERVER_RANK, MPI_COMM_WORLD);
    MPI_Bcast(&diff, 1, MPI_INT, SERVER_RANK, MPI_COMM_WORLD);
    MPI_Bcast(&i, 1, MPI_INT, SERVER_RANK, MPI_COMM_WORLD);

    long nonce1;
    MPI_Status status1;
    MPI_Recv(&nonce1, 1, MPI_LONG, MPI_ANY_SOURCE, TAG_RESULT_NONCE,
             MPI_COMM_WORLD, &status1);
    int rank1 = status1.MPI_SOURCE;

    int flag = 0;
    MPI_Iprobe(MPI_ANY_SOURCE, TAG_RESULT_NONCE, MPI_COMM_WORLD, &flag,
               MPI_STATUS_IGNORE);

    long winning_nonce;
    int winner_rank;

    if (flag) {
      long nonce2;
      MPI_Status status2;
      MPI_Recv(&nonce2, 1, MPI_LONG, MPI_ANY_SOURCE, TAG_RESULT_NONCE,
               MPI_COMM_WORLD, &status2);
      int rank2 = status2.MPI_SOURCE;
      std::cout << "Two blocks mined simultaneously by ranks " << rank1
                << " and " << rank2 << std::endl;

      Block block1(i, "test" + std::to_string(i), prevBlock.hash, diff);
      block1.nonce = nonce1;
      block1.hash = block1.calculateHash(nonce1);

      Block block2(i, "test" + std::to_string(i), prevBlock.hash, diff);
      block2.nonce = nonce2;
      block2.hash = block2.calculateHash(nonce2);

      BlockChain bc1 = bc;
      bc1.addBlock(block1);

      BlockChain bc2 = bc;
      bc2.addBlock(block2);

      std::cout << "Cumulative difficulty for rank " << rank1
                << " block: " << bc1.getCumulativeDifficulty() << std::endl;
      std::cout << "Cumulative difficulty for rank " << rank2
                << " block: " << bc2.getCumulativeDifficulty() << std::endl;

      if (bc1.getCumulativeDifficulty() >= bc2.getCumulativeDifficulty()) {
        std::cout << "Choosing block from rank " << rank1 << std::endl;
        winning_nonce = nonce1;
        winner_rank = rank1;
      } else {
        std::cout << "Choosing block from rank " << rank2 << std::endl;
        winning_nonce = nonce2;
        winner_rank = rank2;
      }
    } else {
      winning_nonce = nonce1;
      winner_rank = rank1;
    }

    Block newBlock(i, "test" + std::to_string(i), prevBlock.hash, diff);
    newBlock.nonce = winning_nonce;
    newBlock.hash = newBlock.calculateHash(winning_nonce);

    if (bc.addBlock(newBlock)) {
      std::cout << "Mined by rank " << winner_rank << ", block " << i
                << " | Hash: " << newBlock.hash
                << " | Nonce: " << newBlock.nonce << " | Difficulty: " << diff
                << std::endl;

      for (int r = 1; r < num_of_processes; ++r) {
        if (r != winner_rank) {
          int stop_signal = 1;
          MPI_Send(&stop_signal, 1, MPI_INT, r, TAG_STOP, MPI_COMM_WORLD);
        }
      }
    } else {
      std::cout << "Block from rank " << winner_rank
                << " was not valid. Retrying." << std::endl;
      i--;
    }
  }

  int stop_signal = -1;
  char dummy_hash[65] = {0};
  MPI_Bcast(dummy_hash, 65, MPI_CHAR, SERVER_RANK, MPI_COMM_WORLD);
  MPI_Bcast(&stop_signal, 1, MPI_INT, SERVER_RANK, MPI_COMM_WORLD);
  MPI_Bcast(&stop_signal, 1, MPI_INT, SERVER_RANK, MPI_COMM_WORLD);

  auto end = std::chrono::high_resolution_clock::now();
  auto durationMs =
      std::chrono::duration_cast<std::chrono::milliseconds>(end - start);
  std::cout << "Time to mine " << blocksToMine << " blocks on " << num_clients
            << " nodes." << std::endl;
  std::cout << durationMs.count() << "ms" << std::endl;
}

void client(int rank, int num_of_processes, unsigned int threadCount) {
  omp_set_num_threads(threadCount);
  const int num_clients = num_of_processes - 1;

  while (true) {
    char prevHash_cstr[65];
    int difficulty, index;

    MPI_Bcast(prevHash_cstr, 65, MPI_CHAR, SERVER_RANK, MPI_COMM_WORLD);
    MPI_Bcast(&difficulty, 1, MPI_INT, SERVER_RANK, MPI_COMM_WORLD);
    MPI_Bcast(&index, 1, MPI_INT, SERVER_RANK, MPI_COMM_WORLD);

    if (index == -1) {
      break;
    }

    Block block(index, "test" + std::to_string(index), prevHash_cstr,
                difficulty);
    std::string target(difficulty, '0');

    bool solution_found = false;
    long nonce_solution = 0;

#pragma omp parallel
    {
      long nonce = (rank - 1) + (omp_get_thread_num() * num_clients);
      int num_threads_in_parallel = omp_get_num_threads();

      while (!solution_found) {
        std::string hash = block.calculateHash(nonce);
        if (hash.substr(0, difficulty) == target) {
#pragma omp critical
          {
            if (!solution_found) {
              solution_found = true;
              nonce_solution = nonce;
            }
          }
        }
        nonce += num_clients * num_threads_in_parallel;

#pragma omp master
        {
          int stop_flag = 0;
          MPI_Iprobe(SERVER_RANK, TAG_STOP, MPI_COMM_WORLD, &stop_flag,
                     MPI_STATUS_IGNORE);
          if (stop_flag) {
            int stop_signal;
            MPI_Recv(&stop_signal, 1, MPI_INT, SERVER_RANK, TAG_STOP,
                     MPI_COMM_WORLD, MPI_STATUS_IGNORE);
#pragma omp critical
            {
              solution_found = true;
            }
          }

          int new_task_flag = 0;
          MPI_Iprobe(SERVER_RANK, TAG_TASK_HASH, MPI_COMM_WORLD, &new_task_flag,
                     MPI_STATUS_IGNORE);
          if (new_task_flag) {
#pragma omp critical
            {
              solution_found = true;
            }
          }
        }
      }
    }

    if (nonce_solution != 0) {
      MPI_Send(&nonce_solution, 1, MPI_LONG, SERVER_RANK, TAG_RESULT_NONCE,
               MPI_COMM_WORLD);
    }
  }
}

int main(int argc, char *argv[]) {
  bool useMPI = false;
  unsigned int threadCount = 0;
  unsigned int blocksToMine = 0;

  for (int i = 1; i < argc; ++i) {
    std::string arg = argv[i];
    if (arg == "-t") {
      if (i + 1 < argc) {
        try {
          threadCount = std::stoul(argv[++i]);
        } catch (...) {
          std::cerr << "Error: invalid thread count" << std::endl;
          return 1;
        }
      } else {
        std::cerr << "Error: -t requires an argument" << std::endl;
        return 1;
      }
    } else if (arg == "-b") {
      if (i + 1 < argc) {
        try {
          blocksToMine = std::stoul(argv[++i]);
        } catch (...) {
          std::cerr << "Error: invalid number of blocks to mine" << std::endl;
          return 1;
        }
      } else {
        std::cerr << "Error: -b requires an argument" << std::endl;
        return 1;
      }
    } else if (arg == "-mpi") {
      useMPI = true;
    } else {
      std::cerr << "Error: unknown argument " << arg << std::endl;
      return 1;
    }
  }

  if (threadCount == 0) {
    std::cerr << "Error: thread count must be > 0" << std::endl;
    std::cerr << "Usage: " << argv[0]
              << " -t <thread_count> -b <blocks_to_mine> [-mpi]" << std::endl;
    return 1;
  }

  if (blocksToMine == 0) {
    std::cerr << "Error: blocks to mine must be > 0" << std::endl;
    std::cerr << "Usage: " << argv[0]
              << " -t <thread_count> -b <blocks_to_mine> [-mpi]" << std::endl;
    return 1;
  }

  if (useMPI) {
    int rank, num_of_processes;

    MPI_Init(&argc, &argv);
    MPI_Comm_size(MPI_COMM_WORLD, &num_of_processes);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    if (rank == SERVER_RANK) {
      server(num_of_processes, blocksToMine);
    } else {
      client(rank, num_of_processes, threadCount);
    }

    MPI_Finalize();
  } else {
    BlockChain bc;
    omp_set_num_threads(threadCount);

    auto start = std::chrono::high_resolution_clock::now();
    for (int i = 1; i <= blocksToMine; i++) {
      const Block &prevBlock = bc.getChain().back();
      int diff = bc.getDifficulty();
      // int diff = prevBlock.difficulty;
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

    std::cout << "Time to mine " << blocksToMine << " on " << threadCount
              << " threads" << std::endl;
    std::cout << durationMs.count() << "ms" << std::endl;
  }
  return 0;
}
