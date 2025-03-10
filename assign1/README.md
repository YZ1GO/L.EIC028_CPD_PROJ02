# CPDPROJ1 - Performance Evaluation of Matrix Multiplication Algorithms: Single-Core and Multi-Core Implementations

## Pre Requirements
To be able to read performance counters on LINUX, you need to edit the following file:

`/proc/sys/kernel/perf_event_paranoid`

and put `-1` instead of `3`.

You can run the following command:
```bash
sudo sh -c 'echo -1 >/proc/sys/kernel/perf_event_paranoid'
```

> [!NOTE]
> You may need to install `libopenmpi-dev` first.

> [!IMPORTANT]
> To run the C# project and fully utilize all functionalities, you need to have [.NET](https://dotnet.microsoft.com/en-us/) installed.

## Installation

[Download PAPI](https://icl.utk.edu/papi/)

1. Uncompress to a directory and change to "src" directory
```bash
./configure 
make 
sudo make install 
sudo ldconfig 
```

2. Compiling tests code: 
```bash
make test 
cd ctests 
./zero.     # Run tests
```

3. Install papi-tools:
```bash
sudo apt-get install papi-tools 
```

Check available counters: `papi_avail`

> [!IMPORTANT] 
> Ensure that `PAPI_L1_DCM` and `PAPI_L2_DCM` display `YES` in the Avail section to proceed.

## Usage

> [!IMPORTANT]
> Change to `src` directory

### matrixproduct.cpp
```bash 
g++ -O2 matrixproduct.cpp -o <fileout> -lpapi -fopenmp

sudo ./<fileout> <file to register data> <op> <size> [<core: 0|1>] [<version: 1|2>] [<block size>]
```

### matrixproduct.cs
```bash
dotnet run <file to register data> <op> <size> [<block size>]
```

***Note:*** We have implemented a block-oriented algorithm in C#, but it was not used for any analysis.

### Automatized bash scripts to run 30 times and register data

Data is registered in `src/data/` folder as `txt` files

> [!WARNING]
> These scripts take a significant amount of time to execute. To save time, consider reducing the number of iterations in the for loops.

```bash
./mult.sh           # mult_cpp.txt and mult_cs.txt
./line_mult.sh      # line_mult_cpp.txt and line_mult_cs.txt
./block_mult.sh     # block_mult.txt
```

Or simply run:

```bash
./run_all.sh        # runs all 3 scripts mentioned above
```

After having the `5 txt` files in `data` folder you can run:
```bash
python3 calc_stats.py
```
That will generate a `statistics.txt` file with all the data previously registered.

## Extra

For the report, we have included several Python programs to generate the necessary graphs. These graphs are saved in the `src/img/` folder. Below is a list of the available scripts and their purposes:

```bash
# Generates a graph comparing the execution time between C++ (single core) and C#
single_core_time_comparison.py

# Generates a graph comparing the execution time between single-core and multi-core implementations in C++
core_comparison_cpp.py 

# Generates graphs comparing the L1 and L2 DCM between single-core and multi-core implementations in C++
cache_comparison_cpp.py

# Generates a graph showing the speedup of multi-core implementations relative to single-core in C++
speedup.py

# Generates a graph showing the MFLOPS (Million Floating Point Operations Per Second) between single-core and multi-core implementations in C++
mflops.py

# Generates a graph showing the efficiency of multi-core (16 cores in our case) relative to single-core in C++
efficiency.py

# Generates a graph comparing the execution time between OnMultLine and OnMultBlock in C++
line_block_time_comparison.py

# Generates graphs comparing the L1 and L2 DCM between OnMultLine and OnMultBlock in C++
line_block_dcm_comparison.py
```

> [!NOTE] 
> The programs use predefined data extracted from `statistics.txt` instead of dynamically reading and parsing the file.

### TIP

You can use virtual environments with [venv](https://docs.python.org/3/library/venv.html) to run the additional programs that utilize matplotlib.

```bash
cd extra/
python3 -m venv venv
source venv/bin/activate

pip install matplotlib

python3 <program.py>

deactivate # to exit the virtual environment
```