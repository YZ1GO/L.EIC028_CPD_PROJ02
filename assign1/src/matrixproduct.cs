using System;
using System.Diagnostics;
using System.IO;

class Program
{
    static void Main(string[] args)
    {
        if (args.Length < 3)
        {
            Console.WriteLine("Usage: <file to register data> <op> <size> [<block size>]");
            Console.WriteLine("op:");
            Console.WriteLine("  1: Basic Row-Column Multiplication");
            Console.WriteLine("  2: Element-Wise Multiplication");
            Console.WriteLine("  3: Block-Oriented Multiplication");
            Console.WriteLine("For op 3, block size is required.");
            return;
        }

        string operation;
        string outputFile = args[0];
        int op = int.Parse(args[1]);
        int size = int.Parse(args[2]);
        int blockSize = 0;

        if (op == 3 && args.Length >= 4)
        {
            blockSize = int.Parse(args[3]);
        }

        double time = 0;
        switch (op)
        {
            case 1:
                time = MatrixProduct.OnMult(size, size);
                operation = "Multiplication";
                break;
            case 2:
                time = MatrixProduct.OnMultLine(size, size);
                operation = "Line Multiplication";
                break;
            case 3:
                time = MatrixProduct.OnMultBlock(size, size, blockSize);
                operation = "Block Multiplication";
                break;
            default:
                Console.WriteLine("Invalid operation.");
                return;
        }

        using (StreamWriter writer = new StreamWriter(outputFile, true))
        {
            writer.WriteLine($"Operation: {operation}");
            writer.WriteLine($"Dimensions: {size}");
            if (op == 3)
            {
                writer.WriteLine($"Block Size: {blockSize}");
            }
            writer.WriteLine($"Time: {time:F3} seconds");
            writer.WriteLine();
        }
    }
}

class MatrixProduct
{
    static double[,] InitializeMatrixOne(int size)
    {
        double[,] pha = new double[size, size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                pha[i, j] = 1.0;

        return pha;
    }

    static double[,] InitializeMatrixTwo(int size)
    {
        double[,] phb = new double[size, size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                phb[i, j] = i + 1;

        return phb;
    }

    // display 10 elements of the result matrix to verify correctness    
    static void DisplayResultMatrix(double[,] res, int size)
    {
        Console.WriteLine("Result matrix:");
        for (int i = 0; i < 1; i++)
        {
            for (int j = 0; j < Math.Min(10, size); j++)
            {
                Console.Write(res[i, j] + " ");
            }
        }
        Console.WriteLine();
    }

    public static double OnMult(int m_ar, int m_br)
    {
        double[,] pha = InitializeMatrixOne(m_ar);
        double[,] phb = InitializeMatrixTwo(m_br);
        double[,] phc = new double[m_ar, m_br];

        Stopwatch stopwatch = Stopwatch.StartNew();

        // Matrix multiplication
        for (int i = 0; i < m_ar; i++)
        {
            for (int j = 0; j < m_br; j++)
            {
                double temp = 0;
                for (int k = 0; k < m_ar; k++)
                {
                    temp += pha[i, k] * phb[k, j];
                }
                phc[i, j] = temp;
            }
        }

        stopwatch.Stop();
        Console.WriteLine($"Time: {stopwatch.Elapsed.TotalSeconds:F3} seconds");

        DisplayResultMatrix(phc, m_br);
        return stopwatch.Elapsed.TotalSeconds;
    }

    public static double OnMultLine(int m_ar, int m_br)
    {
        double[,] pha = InitializeMatrixOne(m_ar);
        double[,] phb = InitializeMatrixTwo(m_br);
        double[,] phc = new double[m_ar, m_br];

        Stopwatch stopwatch = Stopwatch.StartNew();

        for (int i = 0; i < m_ar; i++)
        {
            for (int k = 0; k < m_ar; k++)
            {
                // store the pha[i, k] in a temp variable to reduce the number of mem accesses to pha and improve cache performance
                double temp = pha[i, k];
                for (int j = 0; j < m_br; j++)
                {
                    // Note: C# uses 2D array with built-in indexing unlike C++ that uses 1D array with manual indexing
                    phc[i, j] += temp * phb[k, j];
                }
            }
        }

        stopwatch.Stop();
        Console.WriteLine($"Time: {stopwatch.Elapsed.TotalSeconds:F3} seconds");

        DisplayResultMatrix(phc, m_br);
        return stopwatch.Elapsed.TotalSeconds;
    }

    public static double OnMultBlock(int m_ar, int m_br, int bkSize)
    {
        double[,] pha = InitializeMatrixOne(m_ar);
        double[,] phb = InitializeMatrixTwo(m_br);
        double[,] phc = new double[m_ar, m_br];

        Stopwatch stopwatch = Stopwatch.StartNew();

        for (int ii = 0; ii < m_ar; ii += bkSize)
        {
            for (int kk = 0; kk < m_ar; kk += bkSize)
            {
                for (int jj = 0; jj < m_br; jj += bkSize)
                {
                    for (int i = ii; i < ii + bkSize && i < m_ar; i++)
                    {
                        for (int k = kk; k < kk + bkSize && k < m_ar; k++)
                        {
                            for (int j = jj; j < jj + bkSize && j < m_br; j++)
                            {
                                phc[i, j] += pha[i, k] * phb[k, j];
                            }
                        }
                    }
                }
            }
        }

        stopwatch.Stop();
        Console.WriteLine($"Time: {stopwatch.Elapsed.TotalSeconds:F3} seconds");

        DisplayResultMatrix(phc, m_br);
        return stopwatch.Elapsed.TotalSeconds;
    }
}