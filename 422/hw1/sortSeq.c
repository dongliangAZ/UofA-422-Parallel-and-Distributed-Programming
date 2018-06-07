/*
 * File: sortSeq.c
 * Author: Dong Liang/
 * Purpose: To open and read a file and
 *          store lines of the file to an array of String.
 *	     Use a quicksort to sort lines and print them to stdout.
 */

#include <sys/mman.h> 
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <ctype.h>

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
    int i, p=0;
    if (len <= 1)
    return;

    swapA(args+(len)/2, args+len-1);

    for (i=0;i<len-1;++i)
    {
        if (strcmp(args[i], args[len-1]) < 0)
            swapA(args+i, args+p++);
    }
    swapA(args+p, args+len-1);

    quicksort(args, p++);
    quicksort(args+p, len-p);
}

/*
 *  error --- the error check function
 */

void error(char** argv,FILE *file){
   if(argv[1]==NULL){
   fprintf(stderr,"./SortSeq <fileName>\n");
   exit(1);
   }
     //file = fopen(argv[2],"r");
     if (file == NULL){
     fprintf(stderr,"Unable to open file %s\n", argv[1]);
     fprintf(stderr,"./SortSeq <fileName>\n");
     exit(1);
   }

   return;
}



/*
 * main(argc,argv)
 */
int main(int argc, char** argv){
   FILE *file=fopen(argv[1],"r");
   error(argv,file);
	//Do the error check first

   int i=0;  //The length of the array
   char *temp=(char *)malloc(201*sizeof(char));
   struct timeval tv_begin, tv_end;
   char **array = mmap(NULL, (1000000 *201* (sizeof (char *))),PROT_READ | PROT_WRITE, MAP_ANONYMOUS | MAP_SHARED, -1, 0);;
   file = fopen(argv[1],"r");
	
   while(!feof(file)) {
      fgets(temp,201,file);
      array[i]= malloc(201*sizeof(char));
      strcpy(array[i],temp);
      i++;
   }
   fclose(file);

   gettimeofday(&tv_begin, NULL);
   quicksort(array,i-1);
   gettimeofday(&tv_end, NULL);

   printA(array,i-1);
	//runtime: 0 seconds, 1 microseconds
  
   int time =(int)(tv_end.tv_sec - tv_begin.tv_sec);
   int utime = (time*1000000+(int)(tv_end.tv_usec - tv_begin.tv_usec));
   printf("runtime: %d seconds, %d microseconds\n",time,utime);

   free(temp);
   return 0;
}
