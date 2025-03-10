import matplotlib.pyplot as plt

# Data extracted from statistics.txt for Multiplication
dimensions_multiplication = [600, 1000, 1400, 1800, 2200, 2600, 3000]
cpp_single_core_median_times_multiplication = [0.132, 0.595, 2.209, 5.509, 11.578, 21.650, 36.980]
csharp_single_core_median_times_multiplication = [0.970, 4.428, 12.645, 27.607, 51.109, 86.972, 134.920]

# Data extracted from statistics.txt for Line Multiplication
dimensions_line_multiplication = [600, 1000, 1400, 1800, 2200, 2600, 3000]
cpp_single_core_median_times_line_multiplication = [0.067, 0.277, 0.761, 1.846, 4.360, 7.812, 12.521]
csharp_single_core_median_times_line_multiplication = [0.902, 4.143, 11.329, 24.007, 43.870, 73.273, 112.336]

# Create the plot for Multiplication
plt.figure(figsize=(10, 6))
plt.plot(dimensions_multiplication, cpp_single_core_median_times_multiplication, marker='o', label='C++ Single Core')
plt.plot(dimensions_multiplication, csharp_single_core_median_times_multiplication, marker='o', label='C# Single Core')
plt.title('Basic Multiplication Median Time Comparison')
plt.xlabel('Matrix Dimension')
plt.ylabel('Median Time (seconds)')
plt.legend()
plt.grid(True)
plt.savefig('../img/basic_mult_single_core_time.png')

# Create the plot for Line Multiplication
plt.figure(figsize=(10, 6))
plt.plot(dimensions_line_multiplication, cpp_single_core_median_times_line_multiplication, marker='o', label='C++ Single Core')
plt.plot(dimensions_line_multiplication, csharp_single_core_median_times_line_multiplication, marker='o', label='C# Single Core')
plt.title('Element-wise Multiplication Median Time Comparison')
plt.xlabel('Matrix Dimension')
plt.ylabel('Median Time (seconds)')
plt.legend()
plt.grid(True)
plt.savefig('../img/elem_mult_single_core_time.png')