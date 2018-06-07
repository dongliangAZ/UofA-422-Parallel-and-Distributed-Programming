/*
 * File: sortThread.c
 * Author: Dong Liang/
 * Purpose: To open and read a file and
 *          store lines of the file to an array of String.
 *	    Use a quicksort to sort lines and print them to stdout.
 */


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/time.h>
#include <unistd.h>
#include <sys/wait.h>   
#include <sys/mman.h>
#include <errno.h>
#include <math.h>
#include <pthread.h>

void quicksort(char* args[], int en);
void Threadquicksort(char* lines[], int low, int high, int depth);
void* quicksort_thread(void *init);

struct sort
{
    char** lines;
    int low;
    int high;
    int depth;
};


//A swap function
void swapA(char **arg1, char **arg2)
{
    char *tmp = *arg1;
    *arg1 = *arg2;
    *arg2 = tmp;
}

/*
 * printA(char **) - A function to print Strings from an Array to stout.
 */
void printA(char *a[],int len)
{
    int i = 0;
    for (;i<len;i++){
        printf("%s",a[i]);
	free(a[i]);
	}
}

/*
 *  quicksort---quicksort function for String arrays
 */

/*
 *  error --- the error check function
 */
void error(char** argv,FILE *file){
   if(argv[2]==NULL){
   fprintf(stderr,"./sortThread <num> <fileName>\n");
   exit(1);
   }
  	//file = fopen(argv[2],"r");
   if (file == NULL){
   fprintf(stderr,"Unable to open file %s\n", argv[2]);
   fprintf(stderr,"./sortThread <num> <fileName>\n");
   exit(1);
  }
   return;
}


/*
 *  checkProcess--- the error check of number of processes
 *   		    based on the requirements, 2,4,8,16 are
 *		    accepted.
 */
void checkProcess(int pro){
	
   if(pro==1||pro ==2||pro==4||pro==8||pro==16)
   return;

   fprintf(stderr,"In this case, the number of processes should be 2,4,8 or 16\n");
   exit(1);

}

/*
 *  quicksort---quicksort function for String arrays
 */
void quicksort(char *args[], int len)
{
   if (len <= 1)
        return;
        char *p = args[len/2];
        int left = 0;
        int right = len-1;

        for(;; left++, right--){
    while(strcmp( args[left], p)<0) 
    	left++;

        while(strcmp(args[right],p)>0 )
    		right--;

	if(left>= right) {
    	break;
	} 
	else {
	swapA(args+left,args+right);
	}
   }

   quicksort(args, left);
   quicksort(args + left, len-left);
}



void* Worker(void *args)
{
    struct sort *S = args;
    Threadquicksort(S->lines, S->depth,S->low, S->high);
    return NULL;
}

void Threadquicksort(char *args[], int depth, int m, int n)
{
   if (n>m)
   {
      int p = m+ (n -m)/2;
      char* pivot = args[p];
      swapA(args+p,args+n); 
      int partition = m;
      for (int i=m;i<n;i++)
      {
      	if (strcmp(args[i],pivot) < 0)
        {
        swapA(args+i,args+partition); 
        partition++;
        }
      }
      swapA(args+partition,args+n); 
      p=partition;
      if (depth-- > 0)
      {
      struct sort arg = {args,m, p-1, depth};
      pthread_t thread;
      pthread_create(&thread, NULL, Worker, &arg);
      Threadquicksort(args,depth,p+1,n);
      pthread_join(thread, NULL);
      }
      else
      {
      quicksort(args+m,p-m+1);
      quicksort(args+p,n-p+1);
      }
    }
}

int getDepth(int i){

   switch(i){
	case 2: return 1;
	case 4: return 2;
	case 8: return 3;
	case 16: return 4;
	default:
		fprintf(stderr,"Wrong input thread numbers\n");
		exit(1);
   }

}

/*
 * main(argc,argv)
 */
int main(int argc, char** argv){
   int process = atoi(argv[1]);
   FILE *file=fopen(argv[2],"r");
 
   error(argv,file);
	//Do the error check first

   int i=0;  //The length of the array
   
   char *temp=(char *)malloc(128*sizeof(char));
   struct timeval tv_begin, tv_end;
  
   char **array = mmap(NULL, (500000 * 128* (sizeof (char *))),PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_SHARED, -1, 0);;
   file = fopen(argv[2],"r");
   
	//Open the file and inititalize the array
	//with the size of the file.
   while(!feof(file)) {
      fgets(temp,128,file);
      array[i]= malloc(1024*sizeof(char));
      strcpy(array[i],temp);i++;
   }
   fclose(file);
   checkProcess(process);
   int depth = getDepth(atoi(argv[1]));

   gettimeofday(&tv_begin, NULL);
   
//-------------------------------------------
    Threadquicksort(array,depth,0,i-1);
   
//-----------------------------
   gettimeofday(&tv_end, NULL);
   
   array++;
   printA(array,i-1);

	//runtime: 0 seconds, 1 microseconds

   int time =(int)(tv_end.tv_sec - tv_begin.tv_sec);
   int utime = (time*1000000+(int)(tv_end.tv_usec - tv_begin.tv_usec));
   printf("runtime: %d seconds, %d microseconds\n",time,utime);

   free(temp);
   return 0;
}

