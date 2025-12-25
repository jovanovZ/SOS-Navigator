//
// Created by mihaelko27 on 12/24/25.
//

#include "Block.h"

#include <chrono>
#include <openssl/sha.h>
#include <sstream>
#include <iomanip>

Block::Block(int index, std::string data, std::string prevHash, int diff) {
	this->index = index;
	this->data = std::move(data);
	this->prevHash = std::move(prevHash);
	this->timestamp = std::chrono::system_clock::now();
	this->difficulty = diff;
	this->hash = calculateHash();
	this->nonce = 0;

}

std::string Block::calculateHash() {
	std::ostringstream ss;

	std::string timeStampString = timeStampToString(timestamp);

	std::string input = std::to_string(index) +
		timeStampString +
		data +
		prevHash +
		std::to_string(difficulty) +
		std::to_string(nonce);

	unsigned char digest[SHA256_DIGEST_LENGTH];
	SHA256((unsigned char*)input.c_str(), input.size(), digest);

	ss << std::hex << std::setfill('0');
	for (unsigned char c : digest) {
		ss << std::setw(2) << (int)c;
	}

	return ss.str();
}

bool Block::isBlockValid(Block prevBlock, time_t currTime) {
	if (index == 0) {
		return true;
	}

	if (index != prevBlock.index + 1) {
		return false;
	}

	if (prevHash != prevBlock.hash) {
		return false;
	}

	if (hash != calculateHash()) {
		return false;
	}
	std::time_t blockTime = std::chrono::system_clock::to_time_t(timestamp);
	std::time_t prevBlockTime = std::chrono::system_clock::to_time_t(prevBlock.timestamp);

	if (blockTime > currTime + 60) {
		return false;
	}

	if (blockTime >= prevBlockTime + 60){
		return false;
	}

	return true;
}

std::string Block::toString() {
	std::ostringstream out;
	out << "Index-" << index << "\n";
	out << "Data-" << data << "\n";
	out << "Timestamp-" << timeStampToString(timestamp) << "\n";
	out << "Hash-" << hash << "\n";
	out << "PreviousHash-" << prevHash << "\n";
	out << "Difficulty-" << difficulty << "\n";
	out << "Nonce-" << nonce << "\n";
	return out.str();
}

void Block::mineBlock() {
	// to je treba paralelizirat
	std::string target(difficulty, '0');
	while (true) {
		hash = calculateHash();
		if (hash.substr(0, difficulty) == target) {
			break;
		}
		++nonce;
	}
}

std::string Block::timeStampToString(const std::chrono::system_clock::time_point& time_point) {
	std::time_t time = std::chrono::system_clock::to_time_t(time_point);
	std::tm tm = *std::gmtime(&time);

	char buf[64];
	std::strftime(buf, sizeof(buf), "%d. %m. %Y %H:%M:%S", &tm);
	return std::string(buf);
}

