Korak 1,2 izpusti ce ze mas direktorj build

## LINUX
1. mkdir build
2. cd build 
3. cmake .. 
4. make 
5. ./blockchain -t <num> [-mpi]
6. za zagon z mpi mpiexec --bind-to none -n 2 ./blockchain -t 4 -mpi

## WINDOWS
1. mkdir build 
2. cd build 
3. cmake --build . --config Release 
4. blockchain.exe -t <num> -mpi

## WEB APLIKACIJA

Predno zazenes stran buildni app (to kaj pise odzgoraj)

1. cd web_app
2. ./venv/bin/uvicorn main:app --reload
3. [http://127.0.0.1:8000](http://127.0.0.1:8000)
