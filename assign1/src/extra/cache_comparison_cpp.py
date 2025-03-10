import matplotlib.pyplot as plt

# Data extracted from statistics.txt for Multiplication
dimensions_multiplication = [600, 1000, 1400, 1800, 2200, 2600, 3000]
cpp_single_core_l1_dcm_multiplication = [245544301, 1136573328, 3114205294, 6613042675, 12130600926, 21929313763, 35411434923]
cpp_multi_core_v1_l1_dcm_multiplication = [15523526, 71708427, 207153199, 533926646, 1108467154, 1956744764, 3176131643]
cpp_single_core_l2_dcm_multiplication = [508518, 2160265, 291734709, 560463578, 1135122949, 1814943941, 2752866437]
cpp_multi_core_v1_l2_dcm_multiplication = [28298, 193165, 7519427, 16182720, 32364847, 53121533, 90948410]

# Data extracted from statistics.txt for Element-wise Multiplication
dimensions_line_multiplication = [600, 1000, 1400, 1800, 2200, 2600, 3000, 4096, 6144, 8192, 10240]
cpp_single_core_l1_dcm_line_multiplication = [27458503, 127221262, 351630118, 773236185, 2087098810, 4384632910, 6757194380, 17252323688, 58308625731, 138159007093, 269756931939]
cpp_multi_core_v1_l1_dcm_line_multiplication = [1920536, 9462424, 35225422, 83052228, 161068678, 275472955, 421304430, 1113160703, 3713539382, 8817769257, 17104302325]
cpp_multi_core_v2_l1_dcm_line_multiplication = [4284061, 19985286, 47216068, 92268517, 152228067, 233298489, 326651350, 774460248, 2402034982, 5877055564, 11906933244]
cpp_single_core_l2_dcm_line_multiplication = [99876, 493625, 1626412, 10480026, 43252873, 84188928, 141205255, 325455708, 1268189937, 3080419664, 6250883519]
cpp_multi_core_v1_l2_dcm_line_multiplication = [6437, 70834, 224451, 488839, 964624, 1797237, 2971053, 8323775, 32561437, 80456657, 180776517]
cpp_multi_core_v2_l2_dcm_line_multiplication = [1270231, 5752309, 13308848, 22216570, 36073272, 64091070, 138995164, 361375984, 885653791, 1591444872, 2229224871]

# Create the plot for L1 DCM in Basic Multiplication
plt.figure(figsize=(10, 6))
plt.plot(dimensions_multiplication, cpp_single_core_l1_dcm_multiplication, marker='o', label='C++ Single Core')
plt.plot(dimensions_multiplication, cpp_multi_core_v1_l1_dcm_multiplication, marker='o', label='C++ Multi Core V1')
plt.title('L1 DCM Comparison in Basic Multiplication (C++)')
plt.xlabel('Matrix Dimension')
plt.ylabel('L1 DCM')
plt.legend()
plt.grid(True)
plt.savefig('../img/l1_dcm_basic_mult_cpp_core_comparison.png')

# Create the plot for L2 DCM in Basic Multiplication
plt.figure(figsize=(10, 6))
plt.plot(dimensions_multiplication, cpp_single_core_l2_dcm_multiplication, marker='o', label='C++ Single Core')
plt.plot(dimensions_multiplication, cpp_multi_core_v1_l2_dcm_multiplication, marker='o', label='C++ Multi Core V1')
plt.title('L2 DCM Comparison in Basic Multiplication (C++)')
plt.xlabel('Matrix Dimension')
plt.ylabel('L2 DCM')
plt.legend()
plt.grid(True)
plt.savefig('../img/l2_dcm_basic_mult_cpp_core_comparison.png')

# Create the plot for L1 DCM in Element-wise Multiplication
plt.figure(figsize=(10, 6))
plt.plot(dimensions_line_multiplication, cpp_single_core_l1_dcm_line_multiplication, marker='o', label='C++ Single Core')
plt.plot(dimensions_line_multiplication, cpp_multi_core_v1_l1_dcm_line_multiplication, marker='o', label='C++ Multi Core V1')
plt.plot(dimensions_line_multiplication, cpp_multi_core_v2_l1_dcm_line_multiplication, marker='o', label='C++ Multi Core V2')
plt.title('L1 DCM Comparison in Element-wise Multiplication (C++)')
plt.xlabel('Matrix Dimension')
plt.ylabel('L1 DCM')
plt.legend()
plt.grid(True)
plt.savefig('../img/l1_dcm_elem_mult_cpp_core_comparison.png')

# Create the plot for L2 DCM in Element-wise Multiplication
plt.figure(figsize=(10, 6))
plt.plot(dimensions_line_multiplication, cpp_single_core_l2_dcm_line_multiplication, marker='o', label='C++ Single Core')
plt.plot(dimensions_line_multiplication, cpp_multi_core_v1_l2_dcm_line_multiplication, marker='o', label='C++ Multi Core V1')
plt.plot(dimensions_line_multiplication, cpp_multi_core_v2_l2_dcm_line_multiplication, marker='o', label='C++ Multi Core V2')
plt.title('L2 DCM Comparison in Element-wise Multiplication (C++)')
plt.xlabel('Matrix Dimension')
plt.ylabel('L2 DCM')
plt.legend()
plt.grid(True)
plt.savefig('../img/l2_dcm_elem_mult_cpp_core_comparison.png')