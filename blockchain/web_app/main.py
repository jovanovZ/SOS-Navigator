
import asyncio
import subprocess
import signal
from fastapi import FastAPI, Form, HTTPException
from fastapi.responses import StreamingResponse, FileResponse
import os

app = FastAPI()

process_db = {
    "process": None,
    "log_file": "mining_log.txt"
}

BLOCKCHAIN_EXEC_PATH = "../build/blockchain"

@app.get("/")
async def get_index():
    return FileResponse("index.html")

@app.post("/mine")
async def start_mining(
    blocks_to_mine: int = Form(...),
    thread_count: int = Form(...),
    node_count: int = Form(...)
):
    if process_db["process"] and process_db["process"].poll() is None:
        raise HTTPException(status_code=400, detail="A mining process is already running.")

    if not os.path.exists(BLOCKCHAIN_EXEC_PATH):
        raise HTTPException(status_code=500, detail=f"Blockchain executable not found at {BLOCKCHAIN_EXEC_PATH}. Please compile the C++ project first by running 'cmake . && make' in the 'build' directory.")

    command = [
        "mpiexec", "--bind-to", "none",
        "-n", str(node_count),
        BLOCKCHAIN_EXEC_PATH,
        "-mpi",
        "-b", str(blocks_to_mine),
        "-t", str(thread_count)
    ]

    try:
        with open(process_db["log_file"], "w") as log:
            log.write(f"Starting command: {' '.join(command)}\n\n")

        log_file_handle = open(process_db["log_file"], "a")
        
        process = subprocess.Popen(
            command,
            stdout=log_file_handle,
            stderr=subprocess.STDOUT,
            text=True
        )
        process_db["process"] = process
        process_db["log_file_handle"] = log_file_handle

        return {"message": "Mining process started.", "pid": process.pid}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to start mining process: {str(e)}")


@app.post("/stop")
async def stop_mining():
    process = process_db.get("process")
    if not process or process.poll() is not None:
        raise HTTPException(status_code=400, detail="No mining process is currently running.")

    try:
        process.send_signal(signal.SIGINT)
        return {"message": "Stop signal sent to the mining process."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to stop mining process: {str(e)}")


@app.get("/status")
async def get_status():
    process = process_db.get("process")
    if process and process.poll() is None:
        return {"status": "running", "pid": process.pid}
    
    if process and process.poll() is not None:
        log_handle = process_db.get("log_file_handle")
        if log_handle and not log_handle.closed:
            log_handle.close()
            process_db["log_file_handle"] = None
        process_db["process"] = None
        
    return {"status": "idle"}


async def log_generator():
    try:
        with open(process_db["log_file"], "r") as log_file:
            log_file.seek(0, 2)
            while True:
                line = log_file.readline()
                if not line:
                    status = await get_status()
                    if status["status"] == "idle":
                        yield "data: [PROCESS FINISHED]\n\n"
                        break
                    await asyncio.sleep(0.5)
                    continue
                line = line.rstrip('\n')
                yield f"data: {line}\n\n"
    except FileNotFoundError:
        yield "data: Log file not found. Start a mining process to create it.\n\n"


@app.get("/log-stream")
async def log_stream():
    return StreamingResponse(log_generator(), media_type="text/event-stream")

