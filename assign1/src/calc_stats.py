import os
import statistics

def parse_cpp_file(filepath):
    data = {
        "mult_single": {},
        "mult_multi": {},
        "line_single": {},
        "line_multi_v1": {},
        "line_multi_v2": {},
        "block": {}
    }
    with open(filepath, 'r') as file:
        lines = file.readlines()
        i = 0
        while i < len(lines):
            if "Operation:" in lines[i]:
                operation = lines[i].split(":")[1].strip()
                size = int(lines[i + 1].split(":")[1].strip())
                core = lines[i + 2].split(":")[1].strip()
                
                if operation == "Multiplication":
                    key = "mult_single" if core == "Single Core" else "mult_multi"
                    time = float(lines[i + 3].split(":")[1].strip())
                    l1_dcm = int(lines[i + 4].split(":")[1].strip())
                    l2_dcm = int(lines[i + 5].split(":")[1].strip())
                    i += 7  # Move to the next operation block
                elif operation == "Line Multiplication":
                    if core == "Single Core":
                        key = "line_single"
                        time = float(lines[i + 3].split(":")[1].strip())
                        l1_dcm = int(lines[i + 4].split(":")[1].strip())
                        l2_dcm = int(lines[i + 5].split(":")[1].strip())
                        i += 7  # Move to the next operation block
                    else:
                        # Multi-Core: Check for version
                        version = int(lines[i + 3].split(":")[1].strip())
                        key = f"line_multi_v{version}"  # Use version-specific key
                        time = float(lines[i + 4].split(":")[1].strip())
                        l1_dcm = int(lines[i + 5].split(":")[1].strip())
                        l2_dcm = int(lines[i + 6].split(":")[1].strip())
                        i += 8  # Move to the next operation block
                elif operation == "Block Multiplication":
                    key = "block"
                    block_size = int(lines[i + 2].split(":")[1].strip())
                    size = (size, block_size)  # Store size and block size as a tuple
                    time = float(lines[i + 3].split(":")[1].strip())
                    l1_dcm = int(lines[i + 4].split(":")[1].strip())
                    l2_dcm = int(lines[i + 5].split(":")[1].strip())
                    i += 7  # Move to the next operation block
                
                if size not in data[key]:
                    data[key][size] = {"times": [], "l1_dcm": [], "l2_dcm": []}
                data[key][size]["times"].append(time)
                data[key][size]["l1_dcm"].append(l1_dcm)
                data[key][size]["l2_dcm"].append(l2_dcm)
            else:
                i += 1  # Skip lines that don't contain operation data
    return data

def parse_cs_file(filepath):
    data = {
        "mult_single": {},
        "line_single": {}
    }
    with open(filepath, 'r') as file:
        lines = file.readlines()
        i = 0
        while i < len(lines):
            if "Operation:" in lines[i]:
                operation = lines[i].split(":")[1].strip()
                size = int(lines[i + 1].split(":")[1].strip())
                time = float(lines[i + 2].split(":")[1].strip().split()[0])
                
                if operation == "Multiplication":
                    key = "mult_single"
                elif operation == "Line Multiplication":
                    key = "line_single"
                
                if size not in data[key]:
                    data[key][size] = {"times": []}
                data[key][size]["times"].append(time)
                i += 4  # Move to the next operation block
            else:
                i += 1  # Skip lines that don't contain operation data
    return data

def calculate_statistics(data):
    stats = {}
    for key, sizes in data.items():
        stats[key] = {}
        for size, metrics in sizes.items():
            mean_time = statistics.mean(metrics["times"])
            median_time = statistics.median(metrics["times"])
            if "l1_dcm" in metrics:
                mean_l1_dcm = int(statistics.mean(metrics["l1_dcm"]))  # Convert to integer
                median_l1_dcm = int(statistics.median(metrics["l1_dcm"]))  # Convert to integer
                mean_l2_dcm = int(statistics.mean(metrics["l2_dcm"]))  # Convert to integer
                median_l2_dcm = int(statistics.median(metrics["l2_dcm"]))  # Convert to integer
                stats[key][size] = {
                    "time": (mean_time, median_time),
                    "l1_dcm": (mean_l1_dcm, median_l1_dcm),
                    "l2_dcm": (mean_l2_dcm, median_l2_dcm)
                }
            else:
                stats[key][size] = {
                    "time": (mean_time, median_time)
                }
    return stats

def write_statistics_to_file(stats_cpp, stats_cs, output_file):
    with open(output_file, 'w') as file:
        # Write Multiplication data
        file.write("Multiplication:\n")
        for size in stats_cpp["mult_single"]:
            file.write(f"  Dimension: {size}\n")
            if size in stats_cpp["mult_single"]:
                time_stats = stats_cpp["mult_single"][size]["time"]
                l1_stats = stats_cpp["mult_single"][size]["l1_dcm"]
                l2_stats = stats_cpp["mult_single"][size]["l2_dcm"]
                file.write(f"    C++ Single Core:\n")
                file.write(f"       Mean Time = {time_stats[0]:.3f} seconds, Median Time = {time_stats[1]:.3f} seconds\n")
                file.write(f"       Mean L1 DCM = {l1_stats[0]}, Median L1 DCM = {l1_stats[1]}\n")
                file.write(f"       Mean L2 DCM = {l2_stats[0]}, Median L2 DCM = {l2_stats[1]}\n")
            if size in stats_cpp["mult_multi"]:
                time_stats = stats_cpp["mult_multi"][size]["time"]
                l1_stats = stats_cpp["mult_multi"][size]["l1_dcm"]
                l2_stats = stats_cpp["mult_multi"][size]["l2_dcm"]
                file.write(f"    C++ Multi Core (Version 1):\n")
                file.write(f"       Mean Time = {time_stats[0]:.3f} seconds, Median Time = {time_stats[1]:.3f} seconds\n")
                file.write(f"       Mean L1 DCM = {l1_stats[0]}, Median L1 DCM = {l1_stats[1]}\n")
                file.write(f"       Mean L2 DCM = {l2_stats[0]}, Median L2 DCM = {l2_stats[1]}\n")
            if size in stats_cs["mult_single"]:
                time_stats = stats_cs["mult_single"][size]["time"]
                file.write(f"    C# Single Core:\n")
                file.write(f"       Mean Time = {time_stats[0]:.3f} seconds, Median Time = {time_stats[1]:.3f} seconds\n")
            file.write("\n")

        # Write Line Multiplication data
        file.write("Line Multiplication:\n")
        for size in stats_cpp["line_single"]:
            file.write(f"  Dimension: {size}\n")
            if size in stats_cpp["line_single"]:
                time_stats = stats_cpp["line_single"][size]["time"]
                l1_stats = stats_cpp["line_single"][size]["l1_dcm"]
                l2_stats = stats_cpp["line_single"][size]["l2_dcm"]
                file.write(f"    C++ Single Core:\n")
                file.write(f"       Mean Time = {time_stats[0]:.3f} seconds, Median Time = {time_stats[1]:.3f} seconds\n")
                file.write(f"       Mean L1 DCM = {l1_stats[0]}, Median L1 DCM = {l1_stats[1]}\n")
                file.write(f"       Mean L2 DCM = {l2_stats[0]}, Median L2 DCM = {l2_stats[1]}\n")
            if size in stats_cpp["line_multi_v1"]:
                time_stats = stats_cpp["line_multi_v1"][size]["time"]
                l1_stats = stats_cpp["line_multi_v1"][size]["l1_dcm"]
                l2_stats = stats_cpp["line_multi_v1"][size]["l2_dcm"]
                file.write(f"    C++ Multi Core (Version 1):\n")
                file.write(f"       Mean Time = {time_stats[0]:.3f} seconds, Median Time = {time_stats[1]:.3f} seconds\n")
                file.write(f"       Mean L1 DCM = {l1_stats[0]}, Median L1 DCM = {l1_stats[1]}\n")
                file.write(f"       Mean L2 DCM = {l2_stats[0]}, Median L2 DCM = {l2_stats[1]}\n")
            if size in stats_cpp["line_multi_v2"]:
                time_stats = stats_cpp["line_multi_v2"][size]["time"]
                l1_stats = stats_cpp["line_multi_v2"][size]["l1_dcm"]
                l2_stats = stats_cpp["line_multi_v2"][size]["l2_dcm"]
                file.write(f"    C++ Multi Core (Version 2):\n")
                file.write(f"       Mean Time = {time_stats[0]:.3f} seconds, Median Time = {time_stats[1]:.3f} seconds\n")
                file.write(f"       Mean L1 DCM = {l1_stats[0]}, Median L1 DCM = {l1_stats[1]}\n")
                file.write(f"       Mean L2 DCM = {l2_stats[0]}, Median L2 DCM = {l2_stats[1]}\n")
            if size in stats_cs["line_single"]:
                time_stats = stats_cs["line_single"][size]["time"]
                file.write(f"    C# Single Core:\n")
                file.write(f"       Mean Time = {time_stats[0]:.3f} seconds, Median Time = {time_stats[1]:.3f} seconds\n")
            file.write("\n")

        # Write Block Multiplication data
        file.write("Block Multiplication:\n")
        for size_block in stats_cpp["block"]:
            size, block_size = size_block  # Unpack size and block size
            file.write(f"  Dimension: {size}, Block Size: {block_size}\n")
            if size_block in stats_cpp["block"]:
                time_stats = stats_cpp["block"][size_block]["time"]
                l1_stats = stats_cpp["block"][size_block]["l1_dcm"]
                l2_stats = stats_cpp["block"][size_block]["l2_dcm"]
                file.write(f"    C++:\n")
                file.write(f"       Mean Time = {time_stats[0]:.3f} seconds, Median Time = {time_stats[1]:.3f} seconds\n")
                file.write(f"       Mean L1 DCM = {l1_stats[0]}, Median L1 DCM = {l1_stats[1]}\n")
                file.write(f"       Mean L2 DCM = {l2_stats[0]}, Median L2 DCM = {l2_stats[1]}\n")
            file.write("\n")

def main():
    output_dir = "data"
    cpp_files = {
        "mult_cpp": os.path.join(output_dir, "mult_cpp.txt"),
        "line_mult_cpp": os.path.join(output_dir, "line_mult_cpp.txt"),
        "block_mult": os.path.join(output_dir, "block_mult.txt")
    }
    cs_files = {
        "mult_cs": os.path.join(output_dir, "mult_cs.txt"),
        "line_mult_cs": os.path.join(output_dir, "line_mult_cs.txt")
    }
    output_file = os.path.join(output_dir, "statistics.txt")

    cpp_data = {}
    cs_data = {}

    # Parse C++ files
    for key, filepath in cpp_files.items():
        if os.path.exists(filepath):
            cpp_data[key] = parse_cpp_file(filepath)
        else:
            print(f"Warning: File {filepath} does not exist.")

    # Parse C# files
    for key, filepath in cs_files.items():
        if os.path.exists(filepath):
            cs_data[key] = parse_cs_file(filepath)
        else:
            print(f"Warning: File {filepath} does not exist.")

    # Calculate statistics
    cpp_stats = calculate_statistics(cpp_data.get("mult_cpp", {}))
    line_cpp_stats = calculate_statistics(cpp_data.get("line_mult_cpp", {}))
    block_cpp_stats = calculate_statistics(cpp_data.get("block_mult", {}))

    cs_stats = calculate_statistics(cs_data.get("mult_cs", {}))
    line_cs_stats = calculate_statistics(cs_data.get("line_mult_cs", {}))

    # Combine all C++ stats
    combined_cpp_stats = {
        "mult_single": cpp_stats.get("mult_single", {}),
        "mult_multi": cpp_stats.get("mult_multi", {}),
        "line_single": line_cpp_stats.get("line_single", {}),
        "line_multi_v1": line_cpp_stats.get("line_multi_v1", {}),
        "line_multi_v2": line_cpp_stats.get("line_multi_v2", {}),
        "block": block_cpp_stats.get("block", {})
    }

    # Combine all C# stats
    combined_cs_stats = {
        "mult_single": cs_stats.get("mult_single", {}),
        "line_single": line_cs_stats.get("line_single", {})
    }

    # Write statistics to file
    write_statistics_to_file(combined_cpp_stats, combined_cs_stats, output_file)

if __name__ == "__main__":
    main()