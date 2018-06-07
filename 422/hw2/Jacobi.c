/*
 * Jacobi.c
 * Author: Dong Liang
 * Purpose: C version of jacobi problem
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>
#include <sys/time.h>
#include <pthread.h>
#include <semaphore.h>

typedef struct Workers {
    int threadnum, size, first, last;
    sem_t *max;
} Workers;

int numThread=1;
int numOfIterations =1;
int row = 0;// how many rows a thread has
int tail = 0;// Tail after the division
double E =0.1;
double** grid;
double** Grid;
double maxDiff;
double L = 10.0;
double T = 10.0;
double R = 800.0;
double B = 800.0; 
pthread_barrier_t barrier;
int isDone = 0;

  void error(int argc){
    if( argc < 3 ) {
        fprintf(stderr,"JacobiC <Size> <numThread>\n");
        exit(1);
    }
      if(argc > 8){
        fprintf(stderr,"JacobiC <Size> <numThread> <Left> <Top> <Right> <Bottom> <EPSILON>\n");
        exit(1);
	}
  }

  double maxDouble(double a,double b){
    if(a>b)
      return a;
      return b;
  }

  void arguments(int argc,char *argv[], double l, double t, double r, double b){
    if (argc >= 4)
      l = atof(argv[3]);
    if (argc >= 5)
      t = atof(argv[4]);
    if (argc >= 6)
      r = atof(argv[5]);
    if (argc >= 7)
      b = atof(argv[6]);
    if (argc >= 8)
      E = atof(argv[7]);
  }

  void fileWork(int Size){
    FILE* file = fopen("JacobiResults.txt", "w");
    fprintf(file, "N: %d\n", Size);
    fprintf(file, "Epsilon: %f\n", E);
    fprintf(file, "Number of threads: %d\n", numThread);
    fprintf(file, "Edges: left = %f, right = %f, top = %f, bottom = %f\n",L, R, T, B);

    for( int i = 0; i < Size+2; i++ ) {
      for( int j = 0; j < Size+2; j++ ) {
          fprintf(file, "%7.4f ", grid[i][j]);
        }
        fprintf(file, "\n");
      }
    	fclose(file);
  }


  void* threadWorker (void* arg) {
    Workers *worker = (Workers*)arg;
    int id = worker->threadnum;
    int Size = worker->size;
    int first = worker->first;
    int last = worker->last;
    sem_t *max = worker->max;

    while(!isDone){
      for( int r = first; r < last; r++ ) {
          for( int c = 1; c <= Size; c++ ) {
            Grid[r][c] = (grid[r - 1][c] + grid[r + 1][c]+ grid[r][c - 1] + grid[r][c + 1]) * 0.25;
            }
        }

    double temp = 0.0;
    for( int r = first; r < last; r++ ) {
      for( int c = 1; c <= Size; c++ ) {
        temp = maxDouble(temp, fabs(Grid[r][c] - grid[r][c]));
        }
      }

    sem_wait(max);
    maxDiff = maxDouble(maxDiff, temp);
    sem_post(max);
		
    pthread_barrier_wait(&barrier);

    if( maxDiff < E ){
            isDone = 1;
    }else{      
          if( id == 0 ) 
            numOfIterations++;

          for( int i = first; i < last; i++ ) 
            for( int j = 0; j < (Size + 2); j++ ) 
                    grid[i][j] = Grid[i][j];    
        }  
        
    pthread_barrier_wait(&barrier);
    if( id == 0 ) 
      maxDiff= 0.0;
    pthread_barrier_wait(&barrier);
    }//while loop
    return NULL;
  }

  void iteration(int Size){
   
    sem_t maxS;
    sem_init(&maxS, 0, 1);

    pthread_attr_t attr;
    pthread_attr_init( &attr );
    pthread_attr_setscope( &attr, PTHREAD_SCOPE_SYSTEM );
    pthread_barrier_init(&barrier, NULL, numThread);
    maxDiff=0.0;
    pthread_t threadArr[numThread];
    for( int i = 0; i < numThread; i++ ) {

        int begin = (i * row) + 1;
        int end = begin + row;
        
        Workers *worker = malloc(sizeof(Workers));
        worker->threadnum = i;
        worker->size = Size;
        worker->first = begin;
        worker->last = end;
        worker->max = &maxS;
	
        if( (i + 1) == numThread ) {
            worker->last += tail;
        }
        pthread_create(&(threadArr[i]), &attr, threadWorker, worker);
    }

    for( int i = 0; i < numThread; i++ ) 
        pthread_join(threadArr[i], NULL);
    
    return;
}

/*
 * main function
 */
  int main( int argc, char *argv[] ) {
    error(argc); 
    int Size = atoi( argv[1] );
    numThread = atoi( argv[2] );    
    row = Size / numThread;
    tail = Size % numThread;

    grid = calloc(Size+2, sizeof(double*));
    Grid = calloc(Size+2, sizeof(double*));
  //------Set up the grid  ------------------------
    arguments(argc,argv, L, T, R, B);  
    for( int i = 0; i < Size+2; i++ ) {
       grid[i] = calloc(Size+2, sizeof(double));
       Grid[i] = calloc(Size+2, sizeof(double));
       for( int j = 0; j < Size+2; j++ ) {
           grid[i][j] = 0.0;
           Grid[i][j] = 0.0;
       }
   }

   for( int i = 0; i < Size+2; i++ ) {
       grid[0][i] = T;
       Grid[0][i] = T;
       grid[Size+2 - 1][i] = B;
       Grid[Size+2 - 1][i] = B;
   }
   for( int i = 1; i < Size+2-1; i++ ) {
       grid[i][0] = L;
       Grid[i][0] = L;
       grid[i][Size+2 - 1] = R;
       Grid[i][Size+2 - 1] = R;
   }
   grid[Size+2-1][0] = B;   
  //------------------------------------------------  
  struct timeval tv_begin, tv_end;
  gettimeofday(&tv_begin, NULL);    
  iteration(Size);
  gettimeofday(&tv_end, NULL);
  //------------------------------------------------
  int time = tv_end.tv_sec - tv_begin.tv_sec;
  int utime = tv_end.tv_usec - tv_begin.tv_usec;
  if ( tv_end.tv_usec < tv_begin.tv_usec ) {
        utime+= 1000000;
        time--;
    }
  printf("C version.Size: %d; numWorkers: %d; iterations: %d; runtime:  %d seconds, %d microseconds;\n",
	Size,numThread,numOfIterations,time,utime);	
  fileWork(Size);   
  return 0;
}
