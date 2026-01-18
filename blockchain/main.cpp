#include "BlockChain.h"
#include <cstring>
#include <iostream>
#include <mpi.h>
#include <omp.h>
#include <string>
#include <vector>

const int SERVER_RANK = 0;
const int BLOCK_COUNT = 50;

const int TAG_TASK_HASH = 1;
const int TAG_TASK_DIFF = 2;
const int TAG_TASK_INDEX = 3;
const int TAG_RESULT_NONCE = 4;
const int TAG_STOP = 5;

void server(int num_of_processes) {
    BlockChain bc;
    auto start = std::chrono::high_resolution_clock::now();
    const int num_clients = num_of_processes - 1;

    for (int i = 1; i <= BLOCK_COUNT; i++) {
        const Block &prevBlock = bc.getChain().back();
        // int diff = bc.getDifficulty();
        int diff = prevBlock.difficulty;

        char prevHash_cstr[65];
        strncpy(prevHash_cstr, prevBlock.hash.c_str(), 65);
        MPI_Bcast(prevHash_cstr, 65, MPI_CHAR, SERVER_RANK, MPI_COMM_WORLD);
        MPI_Bcast(&diff, 1, MPI_INT, SERVER_RANK, MPI_COMM_WORLD);
        MPI_Bcast(&i, 1, MPI_INT, SERVER_RANK, MPI_COMM_WORLD);

        long winning_nonce;
        MPI_Status status;
        MPI_Recv(&winning_nonce, 1, MPI_LONG, MPI_ANY_SOURCE, TAG_RESULT_NONCE,
                 MPI_COMM_WORLD, &status);
        int winner_rank = status.MPI_SOURCE;

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
    std::cout << "Time to mine " << BLOCK_COUNT << " blocks on " << num_clients
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

    if (useMPI) {
        int rank, num_of_processes;

        MPI_Init(&argc, &argv);
        MPI_Comm_size(MPI_COMM_WORLD, &num_of_processes);
        MPI_Comm_rank(MPI_COMM_WORLD, &rank);

        if (rank == SERVER_RANK) {
            server(num_of_processes);
        } else {
            client(rank, num_of_processes, threadCount);
        }

        MPI_Finalize();
    } else {
        BlockChain bc;
        omp_set_num_threads(threadCount);

        auto start = std::chrono::high_resolution_clock::now();
        for (int i = 1; i <= BLOCK_COUNT; i++) {
            const Block &prevBlock = bc.getChain().back();
            // int diff = bc.getDifficulty();
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