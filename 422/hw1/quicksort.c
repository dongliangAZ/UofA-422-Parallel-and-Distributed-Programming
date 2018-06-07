#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>



void swapA(char **arg1, char **arg2)
{
    char *tmp = *arg1;
    *arg1 = *arg2;
    *arg2 = tmp;
}



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






void print_list(char *args[], int len)
{
    int i=0;
    for (;i<len;++i)
        printf("%s",args[i]);
}

int main(int argc, char** argv)
{
	FILE *file=fopen(argv[2],"r");
	char *array[128];
	char *temp=(char *)malloc(64*sizeof(char));
	int i =0;
	while(!feof(file)) {
	fgets(temp,64,file);
	array[i]= malloc(64*sizeof(char));
	strcpy(array[i],temp);i++;
   	}



    	quicksort(array, i-1);
    	print_list(array, i-1);
    	
	return 0;
}