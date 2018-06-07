

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.concurrent.Semaphore;

//import Body;
//import Point;
//import GUI;

public class Universe {

	private boolean debug = true;
	private boolean flag = false;
	private int numBodies = 2;
	private long steps = 1000L;
	private double mass = 100.0f;
	private double radius = 10.0;
	private int numberOfCollisionsDetected = 0;

	private Random randomNumberGenerator;
	private Body bodies[];

	private int workerForBodies[][];
	private int numWorkers = 4;
	private int numArrived;
	private Semaphore[] barriers;
	private Semaphore mutex;

	private final double G = 6.67e-11;
	private final double DT = 0.015f;
	private final double MAX = 100;
	private final double MAX_VEL = 300;

	private long startTime, endTime;
	private int seconds, microseconds;

	private GUI gui;

	// sequential program
	public Universe(int numBodies, double radius, long steps, boolean flag, long seed, boolean debug) {
		if (numBodies > 0) {
			this.numBodies = numBodies;
		}
		if (radius > 0) {
			this.radius = radius;
		}
		if (steps > 0) {
			this.steps = steps;
		}
		if (seed != 0) {
			randomNumberGenerator = new Random(seed);
		} else {
			randomNumberGenerator = new Random();
		}
		this.flag = flag;
		this.debug = debug;

		workerForBodies = new int[1][numBodies];
		for (int i = 0; i < numBodies; i++) {
			workerForBodies[0][i] = i;
		}
	}

	// parallel program
	public Universe(int numWorkers, int numBodies, double radius, long steps, boolean flag, long seed, boolean debug) {
		if (numBodies > 0) {
			this.numBodies = numBodies;
		}
		if (radius > 0) {
			this.radius = radius;
		}
		if (steps > 0) {
			this.steps = steps;
		}
		if (numWorkers > 0) {
			this.numWorkers = numWorkers;
		}
		if (seed != 0) {
			randomNumberGenerator = new Random(seed);
		} else {
			randomNumberGenerator = new Random();
		}
		this.flag = flag;
		this.debug = debug;

		// set semaphore
		barriers = new Semaphore[3];
		for (int i = 0; i < barriers.length; i++) {
			barriers[i] = new Semaphore(0);
		}

		mutex = new Semaphore(1);

		workerForBodies = new int[numWorkers][];

		numArrived = 1;

		setWorkerIndex();
	}

	private void setWorkerIndex() {
		int currIndex;
		int division = numBodies / numWorkers;
		boolean firstDo = division % 2 == 0;

		for (int i = 0; i < numWorkers; i++) {
			if (firstDo && i < numBodies % numWorkers)
				workerForBodies[i] = new int[division + 1];
			else if (!firstDo && i >= (numWorkers - numBodies % numWorkers))
				workerForBodies[i] = new int[division + 1];
			else
				workerForBodies[i] = new int[division];
		}

		for (int i = 0; i < numWorkers; i++) {
			currIndex = 0;
			for (int j = 0; j < workerForBodies[i].length; j++) {
				workerForBodies[i][j] = currIndex + (j % 2 == 0 ? i : numWorkers - (i + 1));
				currIndex += numWorkers;
			}
		}
	}

	public void runParallel() {

		generateBodies();

		if (flag) {
			gui = new GUI(this);
		}

		Worker[] workers = new Worker[numWorkers];
		for (int i = 0; i < numWorkers; i++) {
			workers[i] = new Worker(i, this);
		}

		startTime = System.currentTimeMillis();
		for (int i = 0; i < numWorkers; i++) {
			workers[i].start();
		}

		for (int i = 0; i < numWorkers; i++) {
			try {
				workers[i].join();
			} catch (InterruptedException e) {
				System.err.println("ERROR: workers[" + i + "] can not join");
			}
		}

		endTime = System.currentTimeMillis();

		// finally
		seconds = (int) ((endTime - startTime) / 1000);
		microseconds = (int) ((endTime - seconds) % 1000);
		System.out.println("");
		System.out.println("computation time = " + seconds + " seconds, " + microseconds + " microseconds");
		System.out.println("The number of collisions detected: " + numberOfCollisionsDetected);

		if (debug) {
			for (int i = 0; i < bodies.length; i++) {
				System.out.println("x: " + bodies[i].getXPos() + " y:" + bodies[i].getYPos() + " xVel:"
						+ bodies[i].getXVel() + " yVel:" + bodies[i].getYVel());
			}
		}

		writeResult("parallelResult.txt");
	}

	public void runSequential() {

		generateBodies();

		if (flag) {
			gui = new GUI(this);
		}

		startTime = System.currentTimeMillis();

		for (int i = 0; i < steps; i++) {
			sequentialCalculateForces();
			sequentialMoveBodies();
			sequentialDetectCollisions();
			if (flag) {
				gui.updatePlanets(this.bodies);
			}
		}
		endTime = System.currentTimeMillis();

		// finally
		seconds = (int) ((endTime - startTime) / 1000);
		microseconds = (int) ((endTime - seconds) % 1000);
		System.out.println("");
		System.out.println("computation time = " + seconds + " seconds, " + microseconds + " microseconds");
		System.out.println("The number of collisions detected: " + numberOfCollisionsDetected);

		if (debug) {
			for (int i = 0; i < bodies.length; i++) {
				System.out.println("x: " + bodies[i].getXPos() + " y:" + bodies[i].getYPos() + " xVel:"
						+ bodies[i].getXVel() + " yVel:" + bodies[i].getYVel());
			}
		}

		writeResult("sequentialResult.txt");

	}

	private void generateBodies() {
		bodies = new Body[numBodies];

		for (int i = 0; i < bodies.length; i++) {
			if (i == 0) {
				bodies[i] = new Body(mass, radius, new Point(rand(), rand()), new Point(0, 0),
						new Point(randVel(), randVel()));
			} else {
				boolean checkPosition = true;
				double x = rand();
				double y = rand();

				// check every position just have one body
				while (checkPosition) {
					boolean flag = true;
					for (int j = 0; j < i; j++) {
						if (bodies[j].getXPos() == x && bodies[j].getYPos() == y) {
							flag = false;
						}
					}
					if (flag) {
						checkPosition = false;
					}
				}

				bodies[i] = new Body(mass, radius, x, y, 0, 0, randVel(), randVel());
			}
			bodies[i].setId(i);
		}
		if (debug) {
			for (int i = 0; i < bodies.length; i++) {
				System.out.println("x:" + bodies[i].getXPos() + " y:" + bodies[i].getYPos() + " xVel:"
						+ bodies[i].getXVel() + " yVel:" + bodies[i].getYVel());
			}
		}
	}

	private void sequentialCalculateForces() {
		calculateForces(0);
	}

	public void calculateForces(int number) {
		double distance, magnitude;
		Point direction;
		int index;

		for (int i = 0; i < workerForBodies[number].length; i++) {
			index = workerForBodies[number][i];
			for (int j = index + 1; j < numBodies; j++) {
				distance = Math.sqrt((bodies[index].getXPos() - bodies[j].getXPos())
						* (bodies[index].getXPos() - bodies[j].getXPos())
						+ (bodies[index].getYPos() - bodies[j].getYPos())
								* (bodies[index].getYPos() - bodies[j].getYPos()));

				magnitude = G * bodies[index].getMass() * bodies[j].getMass() / (distance * distance);
				direction = new Point(bodies[j].getXPos() - bodies[index].getXPos(),
						bodies[j].getYPos() - bodies[index].getYPos());

				bodies[index].setXForce(bodies[index].getXForce() + magnitude * direction.getX() / distance);
				bodies[j].setXForce(bodies[j].getXForce() - magnitude * direction.getX() / distance);
				bodies[index].setYForce(bodies[index].getYForce() + magnitude * direction.getY() / distance);
				bodies[j].setYForce(bodies[j].getYForce() - magnitude * direction.getY() / distance);
			}
		}
	}

	private void sequentialMoveBodies() {
		moveBodies(0);
	}

	public void moveBodies(int number) {
		int index = 0;
		for (int i = 0; i < workerForBodies[number].length; i++) {
			index = workerForBodies[number][i];

			Point deltaV;
			Point deltaP;

			deltaV = new Point(bodies[index].getXForce() / bodies[index].getMass() * DT,
					bodies[index].getYForce() / bodies[index].getMass() * DT);
			deltaP = new Point((bodies[index].getXVel() + deltaV.getX() / 2) * DT,
					(bodies[index].getYVel() + deltaV.getY() / 2) * DT);

			bodies[index].setXVel(bodies[index].getXVel() + deltaV.getX());
			bodies[index].setYVel(bodies[index].getYVel() + deltaV.getY());

			bodies[index].setXPos(bodies[index].getXPos() + deltaP.getX());
			bodies[index].setYPos(bodies[index].getYPos() + deltaP.getY());

			bodies[index].setXForce(0);
			bodies[index].setYForce(0);
		}

	}

	private void sequentialDetectCollisions() {
		detectCollisions(0);
	}

	public void detectCollisions(int number) {
		double distance;
		int index;

		for (int i = 0; i < workerForBodies[number].length; i++) {
			index = workerForBodies[number][i];
			for (int j = index + 1; j < numBodies; j++) {
				distance = Math.sqrt((bodies[index].getXPos() - bodies[j].getXPos())
						* (bodies[index].getXPos() - bodies[j].getXPos())
						+ (bodies[index].getYPos() - bodies[j].getYPos())
								* (bodies[index].getYPos() - bodies[j].getYPos()));

				if (distance <= (bodies[index].getRadius() + bodies[j].getRadius())) {
					ResolveCollision(index, j);
					numberOfCollisionsDetected++;
				}
			}
		}
	}

	private void ResolveCollision(int indexOne, int indexTwo) {
		double distanceSquared;
		double v1forceX, v1forceY, v2forceX, v2forceY;
		double v1normalX, v1tangentX, v2normalX, v2tangentX;
		double v1normalY, v1tangentY, v2normalY, v2tangentY;
		double diffXPos, diffYPos;

		// distance = b1.radius + b2.radius
		distanceSquared = (bodies[indexOne].getRadius() + bodies[indexTwo].getRadius())
				* (bodies[indexOne].getRadius() + bodies[indexTwo].getRadius());

		// body2.x - body1.x
		diffXPos = bodies[indexTwo].getXPos() - bodies[indexOne].getXPos();
		// body2.y - body1.y
		diffYPos = bodies[indexTwo].getYPos() - bodies[indexOne].getYPos();

		// -----------------------------------------------------
		// Find final normal and tangent vectors for body 1's x
		v1normalX = bodies[indexTwo].getXVel() * diffXPos * diffXPos + bodies[indexTwo].getYVel() * diffXPos * diffYPos;
		v1tangentX = bodies[indexOne].getXVel() * diffYPos * diffYPos
				- bodies[indexOne].getYVel() * diffXPos * diffYPos;

		// Find the final total x vector for body 1
		v1forceX = (v1normalX + v1tangentX) / distanceSquared;

		// -----------------------------------------------------
		// Find final normal and tangent y vectors for body 1
		v1normalY = bodies[indexTwo].getXVel() * diffXPos * diffYPos + bodies[indexTwo].getYVel() * diffYPos * diffYPos;
		v1tangentY = -(bodies[indexOne].getXVel() * diffYPos * diffXPos)
				+ bodies[indexOne].getYVel() * diffXPos * diffXPos;

		// Find the final total y vector for body 1
		v1forceY = (v1normalY + v1tangentY) / distanceSquared;

		// -----------------------------------------------------
		// Find final normal and tangent x vectors for body 2
		v2normalX = bodies[indexOne].getXVel() * diffXPos * diffXPos + bodies[indexOne].getYVel() * diffXPos * diffYPos;
		v2tangentX = bodies[indexTwo].getXVel() * diffYPos * diffYPos
				- bodies[indexTwo].getYVel() * diffXPos * diffYPos;

		// Find the final total x vector for body 1
		v2forceX = (v2normalX + v2tangentX) / distanceSquared;

		// -----------------------------------------------------
		// Find final normal and tangent y vectors for body 2
		v2normalY = bodies[indexOne].getXVel() * diffXPos * diffYPos + bodies[indexOne].getYVel() * diffYPos * diffYPos;
		v2tangentY = -(bodies[indexTwo].getXVel() * diffYPos * diffXPos)
				+ bodies[indexTwo].getYVel() * diffXPos * diffXPos;

		// Find the final total y vector for body 1
		v2forceY = (v2normalY + v2tangentY) / distanceSquared;
		// -----------------------------------------------------

		// update final velocity
		bodies[indexOne].setXVel(v1forceX);
		bodies[indexOne].setYVel(v1forceY);
		bodies[indexTwo].setXVel(v2forceX);
		bodies[indexTwo].setYVel(v2forceY);

	}

	private void writeResult(String filename) {
		File file;
		FileOutputStream fileOutput;
		BufferedWriter bufferWriter;
		try {

			file = new File(filename);
			fileOutput = new FileOutputStream(file);
			bufferWriter = new BufferedWriter(new OutputStreamWriter(fileOutput));

			bufferWriter.write("Final Positions and Velocities:\n");
			for (int i = 0; i < numBodies; i++)
				bufferWriter.write("body " + i + ": \n\t Positions: (" + String.format("%.4f", bodies[i].getXPos())
						+ ", " + String.format("%.4f", bodies[i].getYPos()) + ")\n\t " + "Velocities: ("
						+ String.format("%.4f", bodies[i].getXVel()) + ", " + String.format("%.4f", bodies[i].getYVel())
						+ ")\n");

			bufferWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private double rand() {
		return randomNumberGenerator.nextDouble() * (MAX + MAX) - MAX;
	}

	private double randVel() {
		return randomNumberGenerator.nextDouble() * (MAX_VEL + MAX_VEL) - MAX_VEL;
	}

	public Body[] getBodies() {
		return bodies;
	}

	public void setBodies(Body[] bodies) {
		this.bodies = bodies;
	}

	public int getNumArrived() {
		return numArrived;
	}

	public void setNumArrived(int numArrived) {
		this.numArrived = numArrived;
	}

	public long getSteps() {
		return steps;
	}

	public void setSteps(long steps) {
		this.steps = steps;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public GUI getGui() {
		return gui;
	}

	public void setGui(GUI gui) {
		this.gui = gui;
	}

	public int[][] getWorkerForBodies() {
		return workerForBodies;
	}

	public int getNumWorkers() {
		return numWorkers;
	}

	public void setNumWorkers(int numWorkers) {
		this.numWorkers = numWorkers;
	}

	public void setWorkerForBodies(int[][] workerForBodies) {
		this.workerForBodies = workerForBodies;
	}

	public void aquireMutex() {
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			System.err.println("ERROR: can not aquire Mutex");
		}
	}

	public void releaseMutex() {
		mutex.release();
	}

	public void acquireBarrier(int barrierIndex) {
		try {
			barriers[barrierIndex].acquire();
		} catch (InterruptedException e) {
			System.err.println("ERROR: can not aquire barriers[" + barrierIndex + "]");
		}
	}

	public void releaseBarriers(int barrierIndex) {
		barriers[barrierIndex].release(numWorkers - 1);
	}

}
