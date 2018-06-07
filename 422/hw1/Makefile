CC=gcc

FLAGS= -Wall -g -std=gnu99 

sortSeq: sortSeq.o 
	${CC} ${FLAGS} -o sortSeq sortSeq.o 

sortProcess: sortProcess.o 
	${CC} ${FLAGS} -o sortProcess sortProcess.o

sortThread: sortThread.o 
	${CC} ${FLAGS} -o sortThread sortThread.o  -lpthread

# Utility targets
sortThread.o: sortThread.c
	${CC} ${FLAGS} -c sortThread.c

sortProcess.o: sortProcess.c
	${CC} ${FLAGS} -c sortProcess.c

sortSeq.o: sortSeq.c
	${CC} ${FLAGS} -c sortSeq.c



clean: 
	rm sortSeq sortProcess sortThread *.o 

all: 	sortSeq sortProcess sortThread
