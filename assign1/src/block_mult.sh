#!/bin/bash

# Remove the old executable
rm -f mp

# Compile the C++ program
g++ -O2 matrixproduct.cpp -o mp -lpapi -fopenmp

# Define the sizes and block sizes to test
sizes=(4096 6144 8192 10240)
block_sizes=(128 256 512)

# Define the output directory and file
output_dir="data"
output_file="$output_dir/block_mult.txt"

# Create the output directory if it doesn't exist
mkdir -p $output_dir

# Clear the output file
> $output_file

# Run the tests for C++
for size in "${sizes[@]}"; do
    for block_size in "${block_sizes[@]}"; do
        for run in {1..30}; do
            echo "Running size $size, block size $block_size, run $run"
            sudo ./mp $output_file 3 $size $block_size
        done
    done
done
