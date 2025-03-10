#!/bin/bash

# Remove the old executable
rm -f mp

# Compile the C++ program
g++ -O2 matrixproduct.cpp -o mp -lpapi -fopenmp

# Define the sizes to test
sizes=(600 1000 1400 1800 2200 2600 3000)
extra_sizes=(4096 6144 8192 10240)

# Define the output directory and file
output_dir="data"
output_file_cpp="$output_dir/line_mult_cpp.txt"
output_file_cs="$output_dir/line_mult_cs.txt"

# Create the output directory if it doesn't exist
mkdir -p $output_dir

# Clear the output file
> $output_file_cpp
> $output_file_cs

# Run the tests for C++ (regular sizes)
for size in "${sizes[@]}"; do
    for mode in 0 1; do
        if [ $mode -eq 1 ]; then
            for version in 1 2; do
                for run in {1..30}; do
                    echo "Running C++ size $size, mode $mode, version $version, run $run"
                    sudo ./mp $output_file_cpp 2 $size $mode $version
                done
            done
        else
            for run in {1..30}; do
                echo "Running C++ size $size, mode $mode, run $run"
                sudo ./mp $output_file_cpp 2 $size $mode
            done
        fi
    done
done

# Run the tests for C++ (extra sizes)
for size in "${extra_sizes[@]}"; do
    for mode in 0 1; do
        if [ $mode -eq 1 ]; then
            for version in 1 2; do
                for run in {1..30}; do
                    echo "Running C++ extra size $size, mode $mode, version $version, run $run"
                    sudo ./mp $output_file_cpp 2 $size $mode $version
                done
            done
        else
            for run in {1..30}; do
                echo "Running C++ extra size $size, mode $mode, run $run"
                sudo ./mp $output_file_cpp 2 $size $mode
            done
        fi
    done
done

# Run the tests for C#
for size in "${sizes[@]}"; do
    for run in {1..30}; do
        echo "Running C# size $size, run $run"
        dotnet run $output_file_cs 2 $size
    done
done
