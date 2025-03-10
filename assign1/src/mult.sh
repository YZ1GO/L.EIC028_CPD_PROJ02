#!/bin/bash

# Remove the old executable
rm -f mp

# Compile the C++ program
g++ -O2 matrixproduct.cpp -o mp -lpapi -fopenmp

# Define the sizes to test
sizes=(600 1000 1400 1800 2200 2600 3000)

# Define the output directory and file
output_dir="data"
output_file_cpp="$output_dir/mult_cpp.txt"
output_file_cs="$output_dir/mult_cs.txt"

# Create the output directory if it doesn't exist
mkdir -p $output_dir

# Clear the output file
> $output_file_cpp
> $output_file_cs

# Run the tests for C++
for size in "${sizes[@]}"; do
    for mode in 0 1; do
        for run in {1..30}; do
            echo "Running C++ size $size, mode $mode, run $run"
            sudo ./mp $output_file_cpp 1 $size $mode
        done
    done
done

# Run the tests for C#
for size in "${sizes[@]}"; do
    for run in {1..30}; do
        echo "Running C# size $size, run $run"
        dotnet run $output_file_cs 1 $size
    done
done
