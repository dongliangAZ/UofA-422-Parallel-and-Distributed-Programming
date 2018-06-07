/*
 * File: sortProcess.c
 * Author: Dong Liang/
 * Purpose: To open and read a file and
 *          store lines of the file to an array of String.
 *	    Use a quicksort to sort lines and print them to stdout.
 */

#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <ctype.h>
#include <sys/types.h>
#include <sys/mman.h> 
#include <sys/wait.h>   
#include <string.h> 

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

      if(left>= right)
    	break;
      else 
	swapA(args+left,args+right);
	
}

   quicksort(args, left);
   quicksort(args + left, len-left);
}

/*
 *  error --- the error check function
 */
void error(char** argv,FILE *file){
   if(argv[2]==NULL){
   fprintf(stderr,"./sortProcess <num> <fileName>\n");
   exit(1);
   }
  	//file = fopen(argv[2],"r");
   if (file == NULL){
   fprintf(stderr,"Unable to open file %s\n", argv[2]);
   fprintf(stderr,"./sortProcess <num> <fileName>\n");
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

   fprintf(stderr,"In this case, the number of processes should be 1,2,4,8 or 16\n");
   exit(1);

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
   
   char *temp=(char *)malloc(201*sizeof(char));
   struct timeval tv_begin, tv_end;
   char **array = mmap(NULL, (1000000 *201* (sizeof (char *))),PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_SHARED, -1, 0);;
   file = fopen(argv[2],"r");
   //Open the file and inititalize the array

   while(!feof(file)) {
      fgets(temp,201,file);
      array[i]= malloc(201*sizeof(char));
      strcpy(array[i],temp);i++;
   }
   fclose(file);

   int each = i / process;
   int tail = i % process;

   checkProcess(process);

   gettimeofday(&tv_begin, NULL);
   

//-------------------------------------------
    
   for(int m =0 ; m < process; m++ ) {
   pid_t kidpid = fork();

   if( kidpid < 0 ) {
   fprintf(stderr, "Fork failed!\n");
   exit(1);
   }else if( kidpid == 0 ) {
         if( (m + 1) == process )
              quicksort(array+(m*each),each+tail);
         else
              quicksort(array+(m*each),each);
          exit(getpid());
      }
  }

   int O ;
   for( int n =0; n < process; n++ ) {
        wait(&O);
        if( WIFEXITED(O) ) {
           
        } else {
            
        }
    }

   int merge = process / 2;
   while( merge > 0 ) {
	int Meach = i / (2 * merge);
        int Mtail = i % (2 * merge);

        for( int n=0; n< merge; n++ ) {
               pid_t kidpid = fork();
               if( kidpid < 0 ) {
                   fprintf(stderr, "Fork failed!\n");
                   exit(1);
               } else if( kidpid == 0 ) {
                   int x = 2 * n;
                   quicksort(array+(x*Meach), 2*Meach+Mtail);
                   exit(getpid());
               }
           }

           int P;
         for( int n=0; n < process; n++ ) {
               wait(&P);
               if( WIFEXITED(P) ) {
                  
               } else {
               
               }
           }
           merge /= 2;
       }


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
