import matplotlib.pyplot as plt

# Data extracted from statistics.txt for Line Multiplication (Single Core)
dimensions_line_multiplication = [4096, 6144, 8192, 10240]
cpp_single_core_median_times_line_multiplication = [31.875, 112.038, 268.481, 527.535]

# Data extracted from statistics.txt for Block Multiplication
block_sizes = [128, 256, 512]
cpp_block_median_times_4096 = [25.112, 21.023, 31.341]
cpp_block_median_times_6144 = [82.437, 65.609, 58.210]
cpp_block_median_times_8192 = [210.942, 257.119, 431.779]
cpp_block_median_times_10240 = [379.801, 301.117, 264.962]

# Combine block times into a dictionary for easier plotting
cpp_block_median_times = {
    128: [25.112, 82.437, 210.942, 379.801],
    256: [21.023, 65.609, 257.119, 301.117],
    512: [31.341, 58.210, 431.779, 264.962]
}

# Create the plot for comparison
plt.figure(figsize=(10, 6))
plt.plot(dimensions_line_multiplication, cpp_single_core_median_times_line_multiplication, marker='o', label='Line Multiplication Single Core')

for block_size, times in cpp_block_median_times.items():
    plt.plot(dimensions_line_multiplication, times, marker='o', label=f'Block Multiplication Block Size {block_size}')

plt.title('Time Comparison of Element-wise Multiplication (C++ Single Core) and Block Multiplication (C++)')
plt.xlabel('Matrix Dimension')
plt.ylabel('Median Time (seconds)')
plt.legend()
plt.grid(True)
plt.savefig('../img/time_comparison_elem_block_multipl_cpp.png')