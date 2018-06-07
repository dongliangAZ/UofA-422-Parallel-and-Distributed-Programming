The source code files are in three directories(packages): controller, view, model!

In this file, some commands we used to test our program are listed below.
For those commands, we use them for some special case.
For the regular tests, we have a bash script which is included in the report.

usage: java controller.Run <numWorkers> <numBodies> <radius> <steps> | <gui> <seed> <debug>");



commands:
	java controller.Run
-Error handling check.

	java controller.Run 1
-Error handling check.

	java controller.Run 2 100
-Error handling check.

	java controller.Run 8 100 10 1000
-This is a normal/general case.
	
	java controller.Run 33 100 10 1000
-Use this command to test if the number of workers is over 32.
In this case, we design the program just ignores and run as num of workers is 32.


	java controller.Run 16 100 10 1000 true 0.1 false
-Take a look at the GUI

	java controller.Run 2 2 10 1000 true 0.1 false
-Observe how 2 bodies would work.



