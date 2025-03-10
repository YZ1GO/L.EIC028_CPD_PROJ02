import matplotlib.pyplot as plt

# Data extracted from statistics.txt for Multiplication
dimensions_multiplication = [600, 1000, 1400, 1800, 2200, 2600, 3000]
cpp_single_core_times_multiplication = [0.132, 0.595, 2.209, 5.509, 11.578, 21.650, 36.980]
cpp_multi_core_v1_times_multiplication = [0.015, 0.070, 0.231, 0.846, 2.298, 4.310, 6.969]

# Data extracted from statistics.txt for Line Multiplication
dimensions_line_multiplication = [600, 1000, 1400, 1800, 2200, 2600, 3000, 4096, 6144, 8192, 10240]
cpp_single_core_times_line_multiplication = [0.067, 0.277, 0.761, 1.846, 4.360, 7.812, 12.521, 31.875, 112.038, 268.481, 527.535]
cpp_multi_core_v1_times_line_multiplication = [0.011, 0.048, 0.141, 0.335, 0.643, 1.089, 1.692, 4.394, 15.100, 36.181, 71.327]
cpp_multi_core_v2_times_line_multiplication = [0.124, 0.357, 0.879, 1.964, 3.350, 5.088, 7.527, 19.287, 70.128, 123.999, 229.462]

# Calculate MFLOPS for Multiplication
mflops_single_core_multiplication = [(2 * dim**3) / (time * 1e6) for dim, time in zip(dimensions_multiplication, cpp_single_core_times_multiplication)]
mflops_multi_core_v1_multiplication = [(2 * dim**3) / (time * 1e6) for dim, time in zip(dimensions_multiplication, cpp_multi_core_v1_times_multiplication)]

# Calculate MFLOPS for Line Multiplication
mflops_single_core_line_multiplication = [(2 * dim**3) / (time * 1e6) for dim, time in zip(dimensions_line_multiplication, cpp_single_core_times_line_multiplication)]
mflops_multi_core_v1_line_multiplication = [(2 * dim**3) / (time * 1e6) for dim, time in zip(dimensions_line_multiplication, cpp_multi_core_v1_times_line_multiplication)]
mflops_multi_core_v2_line_multiplication = [(2 * dim**3) / (time * 1e6) for dim, time in zip(dimensions_line_multiplication, cpp_multi_core_v2_times_line_multiplication)]

# Create the plot for MFLOPS in Multiplication
plt.figure(figsize=(10, 6))
plt.plot(dimensions_multiplication, mflops_single_core_multiplication, marker='o', label='C++ Single Core')
plt.plot(dimensions_multiplication, mflops_multi_core_v1_multiplication, marker='o', label='C++ Multi Core V1')
plt.title('MFLOPS in Basic Multiplication (C++)')
plt.xlabel('Matrix Dimension')
plt.ylabel('MFLOPS')
plt.legend()
plt.grid(True)
plt.savefig('../img/mflops_basic_mult_cpp.png')

# Create the plot for MFLOPS in Line Multiplication
plt.figure(figsize=(10, 6))
plt.plot(dimensions_line_multiplication, mflops_single_core_line_multiplication, marker='o', label='C++ Single Core')
plt.plot(dimensions_line_multiplication, mflops_multi_core_v1_line_multiplication, marker='o', label='C++ Multi Core V1')
plt.plot(dimensions_line_multiplication, mflops_multi_core_v2_line_multiplication, marker='o', label='C++ Multi Core V2')
plt.title('MFLOPS in Element-wise Multiplication (C++)')
plt.xlabel('Matrix Dimension')
plt.ylabel('MFLOPS')
plt.legend()
plt.grid(True)
plt.savefig('../img/mflops_elem_mult_cpp.png')