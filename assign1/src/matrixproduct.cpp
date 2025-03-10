#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>
#include <fstream>
#include <string>
#include <omp.h>

using namespace std;

#define SYSTEMTIME clock_t


double OnMult(int m_ar, int m_br, bool multi) 
{
	
	SYSTEMTIME Time1, Time2;
	double multStartTime, multEndTime;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	
    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

	int num_threads = 1;
	
	if (multi) {
		multStartTime = omp_get_wtime();
		# pragma omp parallel
		{
			# pragma omp single
            num_threads = omp_get_num_threads();

			# pragma omp for
			for(i=0; i<m_ar; i++)
			{	
				for(j=0; j<m_br; j++)
				{	
					temp = 0;
					for(k=0; k<m_ar; k++)
					{	
						temp += pha[i*m_ar+k] * phb[k*m_br+j];
					}
					phc[i*m_ar+j]=temp;
				}
			}
		}
		multEndTime = omp_get_wtime();
	} 
	else 
	{
		Time1 = clock();
		for(i=0; i<m_ar; i++)
		{	
			for(j=0; j<m_br; j++)
			{	
				temp = 0;
				for(k=0; k<m_ar; k++)
				{	
					temp += pha[i*m_ar+k] * phb[k*m_br+j];
				}
				phc[i*m_ar+j]=temp;
			}
		}
		Time2 = clock();
	}

	double time = multi 
				? (multEndTime - multStartTime) 
				: (double)(Time2 - Time1) / CLOCKS_PER_SEC;

	sprintf(st, "Time: %3.3f seconds\n", time);
	cout << st;
	cout << "Number of threads: " << num_threads << endl;
	
	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);
	
	return time;
}

int lineMultiCoreVersion1(int i, int j, int k, double *pha, double *phb, double *phc, int m_ar, int m_br, int num_threads) {
    # pragma omp parallel
    {
        # pragma omp single
        num_threads = omp_get_num_threads();

        # pragma omp for
        for(i = 0; i < m_ar; i++) {    
            for(k = 0; k < m_ar; k++) {    
                for(j = 0; j < m_br; j++) {    
                    phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
                }
            }
        }
    }

	return num_threads;
}

int lineMultiCoreVersion2(int i, int j, int k, double *pha, double *phb, double *phc, int m_ar, int m_br, int num_threads) {
	# pragma omp parallel shared(pha, phb, phc, m_ar, m_br) private(i, k)
	{
		# pragma omp single
		num_threads = omp_get_num_threads();
		
        for(i = 0; i < m_ar; i++) {    
            for(k = 0; k < m_ar; k++) {    
				# pragma omp for
                for(j = 0; j < m_br; j++) {    
                    phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
                }
            }
        }
	}

	return num_threads;
}

// add code here for line x line matrix multiplication
double OnMultLine(int m_ar, int m_br, bool multi, int version)
{
	SYSTEMTIME Time1, Time2;
	double multStartTime, multEndTime;

	char st[100];
	int i, j, k;

	double *pha, *phb, *phc;
	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phb[i*m_br + j] = (double)(i+1);

	for(i=0; i<m_br; i++)
		for(j=0; j<m_br; j++)
			phc[i*m_br + j] = (double)0.0;

	int num_threads = 1;

	if (multi) 
	{
		multStartTime = omp_get_wtime();
        if (version == 1) {
            num_threads = lineMultiCoreVersion1(i, j, k, pha, phb, phc, m_ar, m_br, num_threads);
        } else if (version == 2) {
            num_threads = lineMultiCoreVersion2(i, j, k, pha, phb, phc, m_ar, m_br, num_threads);
        }
		multEndTime = omp_get_wtime();
	} 
	else 
	{
		Time1 = clock();
		for(i=0; i<m_ar; i++)
		{	
			for(k=0; k<m_ar; k++)
			{	
				for(j=0; j<m_br; j++)
				{	
					phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
				}
			}
		}
		Time2 = clock();
	}

	double time = multi 
				? (multEndTime - multStartTime) 
				: (double)(Time2 - Time1) / CLOCKS_PER_SEC;

	sprintf(st, "Time: %3.3f seconds\n", time);
	cout << st;
	cout << "Number of threads: " << num_threads << endl;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{	for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

    free(pha);
    free(phb);
    free(phc);

	return time;
}

// add code here for block x block matrix multiplication
double OnMultBlock(int m_ar, int m_br, int bkSize)
{
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	int i, j, k, ii, jj, kk;

	double *pha, *phb, *phc;
	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

    for(i=0; i<m_ar; i++)
        for(j=0; j<m_ar; j++)
            pha[i*m_ar + j] = (double)1.0;

    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phb[i*m_br + j] = (double)(i+1);
    
    for(i=0; i<m_br; i++)
        for(j=0; j<m_br; j++)
            phc[i*m_br + j] = (double)0.0;

	Time1 = clock();


	for(ii=0; ii<m_ar; ii+=bkSize) {    
		for(kk=0; kk<m_ar; kk+=bkSize){ 
			for(jj=0; jj<m_br; jj+=bkSize) {
				for (i = ii ; i < ii + bkSize && i < m_ar; i++) {    
					for (k = kk ; k < kk + bkSize && k < m_ar; k++) {
						for (j = jj ; j < jj + bkSize && j < m_br; j++) {
							phc[i*m_ar+j] += pha[i*m_ar+k] * phb[k*m_br+j];
						}
					}
				}
			}
		}
	}

	Time2 = clock();
	double time = (double)(Time2 - Time1) / CLOCKS_PER_SEC;
	sprintf(st, "Time: %3.3f seconds\n", time);
	cout << st;

	// display 10 elements of the result matrix to verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<1; i++)
	{    for(j=0; j<min(10,m_br); j++)
			cout << phc[j] << " ";
	}
	cout << endl;

	free(pha);
	free(phb);
	free(phc);
    
	return time;
}



void handle_error (int retval)
{
    printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
    exit(1);
}

void init_papi() {
    int retval = PAPI_library_init(PAPI_VER_CURRENT);
    if (retval != PAPI_VER_CURRENT && retval < 0) {
        printf("PAPI library version mismatch!\n");
        exit(1);
    }
    if (retval < 0) handle_error(retval);

    std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
        	  << " MINOR: " << PAPI_VERSION_MINOR(retval)
        	  << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

void showUsage() {
    cout << "Usage: <file to register data> <op> <size> [<core: 0|1>] [<version: 1|2>] [<block size>]" << endl;
    cout << "op:" << endl;
    cout << "  1: Basic Row-Column Multiplication" << endl;
    cout << "  2: Element-Wise Multiplication" << endl;
    cout << "  3: Block-Oriented Multiplication" << endl;
    cout << "For op 1 and 2, core is required (0 for single-core, 1 for multi-core)." << endl;
    cout << "For op 2 with multi-core, version is required (1 or 2)." << endl;
    cout << "For op 3, block size is required." << endl;
}

int main (int argc, char *argv[])
{	
	if (argc < 4) {
        showUsage();
        return 1;
    }

	int EventSet = PAPI_NULL;
	long long values[2];
	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;


	ofstream file;
	file.open(argv[1], std::ios::app); //append mode
	
	string operation, core;
	int op = atoi(argv[2]);
	int lin = atoi(argv[3]);
	bool multi;
	int version = 0;
	int blockSize = 0;

	if (op == 1 || op == 2)
	{
		if (argc < 5 || (atoi(argv[4]) != 0 && atoi(argv[4]) != 1)) 
		{
            showUsage();
            return 1;
        }
		multi = atoi(argv[4]);
		core = multi ? "Multi-Core" : "Single Core";
		if (op == 2 && multi == 1) 
		{
            if (argc < 6 || (atoi(argv[5]) != 1 && atoi(argv[5]) != 2)) 
			{
                showUsage();
                return 1;
            }
			version = atoi(argv[5]);
		}
	}
	else if (op == 3) 
	{
        if (argc < 5) 
		{
            showUsage();
            return 1;
        }
        blockSize = atoi(argv[4]);
    } 
	else 
	{
        showUsage();
        return 1;
    }


	// Start counting
	ret = PAPI_start(EventSet);
	if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;
	
	double time;
	switch(op) 
	{
		case 1: 
			time=OnMult(lin, lin, multi);
			operation = "Multiplication";
			break;
		case 2:
			time=OnMultLine(lin, lin, multi, version);  
			operation = "Line Multiplication";
			break;
		case 3:
			time=OnMultBlock(lin, lin, blockSize);  
			operation = "Block Multiplication";
			break;
	}

	ret = PAPI_stop(EventSet, values);
	if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
	printf("L1 DCM: %lld \n",values[0]);
	printf("L2 DCM: %lld \n",values[1]);
	

	file << "Operation: " << operation << "\n";
	file << "Dimensions: " << lin << "\n";
	if (op == 1 || op == 2) file << "Core: " << core << "\n";
	if (op == 2 && multi == 1) file << "Version: " << version << "\n";
	if (op == 3) file << "Block Size: " << blockSize << "\n";
	file << "Time: " << time << "\n";
	file << "L1 DCM: " <<  values[0] << "\n";
	file << "L2 DCM: " <<  values[1] << "\n";
	file << "\n";
	file.close();
	
	
	ret = PAPI_reset( EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL reset" << endl; 
	
	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;

	return 0;
}