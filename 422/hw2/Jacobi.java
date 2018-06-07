
/*
 * Jacobi.java
 * Author: Dong Liang
 * Purpose: Java version of jacobi problem
 */

import java.util.concurrent.Semaphore;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import static java.lang.Math.*;

public class Jacobi {
	private static double[][] grid, Grid;
	private static int numThreads, iterationCount = 1;
	private static int Size = 0;
	private static int row = 0;// how many rows a thread has
	private static int tail = 0;// Tail after the division
	private static File file;
	private static double E = 0.10;
	private static double maxDiff = 0.0;
	private static boolean done = false;

	public static void main(String[] args) {
		errorCheck(args);
		double L = 10.0;
		double T = 10.0;
		double R = 800.0;
		double B = 800.0;

		Size = Integer.parseInt(args[0]);
		numThreads = Integer.parseInt(args[1]);
		row = Size / numThreads;
		tail = Size % numThreads;
		grid = new double[Size + 2][Size + 2];
		Grid = new double[Size + 2][Size + 2];
		// ------------------------------------------------------------
		arguments(args, L, T, R, B);
		setupGrids(grid, Grid, L, T, R, B);
		// ------------------------------------------------------------
		long timerStart = System.nanoTime();
		jacobiIteration(numThreads, row, tail, Size);
		long timerStop = System.nanoTime();
		// ------------------------------------------------------------
		fileWriter(file, Size, L, T, R, B, E, grid);
		System.out.printf(
				"Java version. Size: %d ;numProcs: %d; iterations: %d; runtime: %d seconds, %d microseconds;\n", Size,
				numThreads, iterationCount, (timerStop - timerStart) / 1000000000, (timerStop - timerStart) / 1000);
	}

	private static class Worker extends Thread {
		int Size, firstRow, lastRow, id;
		Semaphore maxSemaphore, threadSemaphore, iterationSemaphore;

		public Worker(int i, int size, int first, int last, Semaphore maxSemaphore, Semaphore threadSemaphore,
				Semaphore iterationSemaphore) {
			this.id = i;
			this.Size = size;
			this.firstRow = first;
			this.lastRow = last;
			this.maxSemaphore = maxSemaphore;
			this.threadSemaphore = threadSemaphore;
			this.iterationSemaphore = iterationSemaphore;
		}

		@Override
		public void run() {
			while (!done) {
				try {
					iterationSemaphore.acquire();
				} catch (InterruptedException ie) {
					System.err.printf("Error occurs as acquiring semaphore!\n");
					System.exit(1);
				}

				for (int r = firstRow; r < lastRow; r++) {
					for (int c = 1; c <= Size; c++) {
						Grid[r][c] = (grid[r - 1][c] + grid[r + 1][c] + grid[r][c - 1] + grid[r][c + 1]) * 0.25;
					}
				}

				double temp = 0.0;
				for (int r = firstRow; r < lastRow; r++) {
					for (int c = 1; c <= Size; c++) {
						temp = max(temp, abs(Grid[r][c] - grid[r][c]));
					}
				}

				try {
					maxSemaphore.acquire();
					maxDiff = max(maxDiff, temp);
					maxSemaphore.release();
				} catch (InterruptedException ie) {
					System.err.println("Max difference calculation interrupted");
					System.exit(1);
				}

				try {

					if (id == 0) {

						while (iterationSemaphore.availablePermits() != (numThreads - 1)) {
							Thread.sleep(1);
						}

						if (maxDiff < E) {
							done = true;
						} else {

							maxDiff = 0.0;
							iterationCount++;
						}

						iterationSemaphore.release();
						while (iterationSemaphore.availablePermits() != numThreads) {
						}
					} else {
						iterationSemaphore.release();

						while (iterationSemaphore.availablePermits() != numThreads) {
						}
					}
					if (!done) {
						for (int i = firstRow; i < lastRow; i++) {
							System.arraycopy(Grid[i], 0, grid[i], 0, grid.length);
						}
					}
				} catch (InterruptedException ie) {
					System.err.printf("Error occurs as waiting at barrier in thread %d", id);
					System.exit(1);
				}
			}
			threadSemaphore.release();
		}
	}

	private static void fileWriter(File file, int Size, double l, double t, double r, double b, double e,
			double[][] grid) {
		file = new File("JacobiResults.txt");
		try (PrintWriter out = new PrintWriter(file)) {
			out.printf("N: %d\n", Size);
			out.printf("Epsilon: %f\n", e);
			out.printf("Number of threads: %d\n", numThreads);
			out.printf("Edges: left = %f, right = %f, top = %f, bottom = %f\n", l, r, t, b);

			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid.length; j++) {
					out.printf("%7.4f ", grid[i][j]);
				}
				out.println();
			}
		} catch (IOException ioe) {
			System.err.printf("Cannot write to JavaJacobiResults.txt\n");
		}
	}

	private static void jacobiIteration(int numThreads, int row, int tail, int Size) {
		Semaphore maxSemaphore = new Semaphore(1);
		Semaphore threadSemaphore = new Semaphore(numThreads);
		Semaphore iterationSemaphore = new Semaphore(numThreads);
		// -------------------------------------
		for (int i = 0; i < numThreads; i++) {
			try {
				threadSemaphore.acquire();
			} catch (InterruptedException ie) {
				System.err.printf("Error while creating thread %d! Exiting...\n", i);
				System.exit(1);
			}
			Worker worker;
			int begin = (i * row) + 1;
			int end = begin + row;

			if ((i + 1) == numThreads)
				worker = new Worker(i, Size, begin, end + tail, maxSemaphore, threadSemaphore, iterationSemaphore);
			else
				worker = new Worker(i, Size, begin, end, maxSemaphore, threadSemaphore, iterationSemaphore);
			worker.start();
		}
		while (threadSemaphore.availablePermits() != numThreads) {
		}
	}

	private static void setupGrids(double[][] g, double[][] G, double l, double t, double r, double b) {
		for (int i = 0; i < g.length; i++) {
			g[0][i] = t;
			G[0][i] = t;

			g[g.length - 1][i] = b;
			G[g.length - 1][i] = b;
		}

		for (int i = 1; i < g.length - 1; i++) {
			g[i][0] = l;
			G[i][0] = l;

			g[i][g.length - 1] = r;
			G[i][g.length - 1] = r;
		}
		g[g.length - 1][0] = b;
	}

	private static void arguments(String[] args, double l, double t, double r, double b) {
		if (args.length >= 3)
			l = Double.parseDouble(args[2]);
		if (args.length >= 4)
			t = Double.parseDouble(args[3]);
		if (args.length >= 5)
			r = Double.parseDouble(args[4]);
		if (args.length >= 6)
			b = Double.parseDouble(args[5]);
		if (args.length >= 7)
			E = Double.parseDouble(args[6]);
	}

	private static void errorCheck(String[] args) {
		if (args.length < 2) {
			System.err.println("JacobiJava <Size> <numThread>");
			System.exit(1);
		} else {
			if (args.length > 7) {
				System.err.println("JacobiJava <gridSize> <numThreads> <Left> <Top> <Right> <Bottom> <EPSILON>");
				System.exit(1);
			}
		}

	}
}
