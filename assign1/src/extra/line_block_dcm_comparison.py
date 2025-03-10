import matplotlib.pyplot as plt

# Data extracted from statistics.txt for Line Multiplication (Single Core)
dimensions = [4096, 6144, 8192, 10240]
cpp_single_core_l1_dcm = [17252323688, 58308625731, 138159007093, 269756931939]
cpp_single_core_l2_dcm = [325455708, 1268189937, 3080419664, 6250883519]

# Data extracted from statistics.txt for Block Multiplication
block_sizes = [128, 256, 512]
cpp_block_l1_dcm_4096 = [11757895561, 9418270490, 9600598999]
cpp_block_l1_dcm_6144 = [39173019894, 31141977419, 30464616176]
cpp_block_l1_dcm_8192 = [98055919695, 84222634938, 83536079043]
cpp_block_l1_dcm_10240 = [181355182621, 144663802923, 141047037021]

cpp_block_l2_dcm_4096 = [2986526858, 1662615571, 1703110627]
cpp_block_l2_dcm_6144 = [9775451362, 5180950941, 3181679219]
cpp_block_l2_dcm_8192 = [26001909210, 22314528914, 19244220956]
cpp_block_l2_dcm_10240 = [45375793112, 24317649833, 14625573775]

# Combine block times into a dictionary for easier plotting
cpp_block_l1_dcm = {
    128: [11757895561, 39173019894, 98055919695, 181355182621],
    256: [9418270490, 31141977419, 84222634938, 144663802923],
    512: [9600598999, 30464616176, 83536079043, 141047037021]
}

cpp_block_l2_dcm = {
    128: [2986526858, 9775451362, 26001909210, 45375793112],
    256: [1662615571, 5180950941, 22314528914, 24317649833],
    512: [1703110627, 3181679219, 19244220956, 14625573775]
}

# Create the plot for L1 DCM comparison
plt.figure(figsize=(10, 6))
plt.plot(dimensions, cpp_single_core_l1_dcm, marker='o', label='Element-wise Multiplication Single Core')

for block_size, l1_dcm in cpp_block_l1_dcm.items():
    plt.plot(dimensions, l1_dcm, marker='o', label=f'Block Multiplication Block Size {block_size}')

plt.title('L1 DCM Comparison Element-wise and Block Multiplication (C++ Single Core)')
plt.xlabel('Matrix Dimension')
plt.ylabel('L1 DCM')
plt.legend()
plt.grid(True)
plt.savefig('../img/l1_dcm_comparison_elem_block.png')

# Create the plot for L2 DCM comparison
plt.figure(figsize=(10, 6))
plt.plot(dimensions, cpp_single_core_l2_dcm, marker='o', label='Element-wise Multiplication Single Core')

for block_size, l2_dcm in cpp_block_l2_dcm.items():
    plt.plot(dimensions, l2_dcm, marker='o', label=f'Block Multiplication Block Size {block_size}')

plt.title('L2 DCM Comparison Element-wise and Block Multiplication (C++ Single Core)')
plt.xlabel('Matrix Dimension')
plt.ylabel('L2 DCM')
plt.legend()
plt.grid(True)
plt.savefig('../img/l2_dcm_comparison_elem_block.png')