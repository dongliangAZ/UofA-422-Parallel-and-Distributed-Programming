

public class Run {

	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println(
					"usage: java controller.Run <numWorkers> <numBodies> <radius> <steps> | <gui> <seed> <debug>");
			System.exit(0);
		}

		int numBodies = 10;
		long steps = 200;
		double radius = 2;
		int numWorkers = 1;
		boolean gui = false;
		long seed = 1510000921029l;
		boolean debug = false;

		if (args.length >= 4) {
			numWorkers = Integer.parseInt(args[0]);
			numBodies = Integer.parseInt(args[1]);
			radius = Double.parseDouble(args[2]);
			steps = Long.parseLong(args[3]);
		}

		if (args.length >= 5) {
			gui = Boolean.parseBoolean(args[4]);
		}

		if (args.length >= 6) {
			seed = Long.parseLong(args[5]);
		}

		if (args.length >= 7) {
			debug = Boolean.parseBoolean(args[6]);
		}

		System.out.println("numWorkers:" + numWorkers + " numBodies: " + numBodies + " radius:" + radius + " steps:"
				+ steps + " gui:" + gui + " seed: " + seed + " debug:" + debug);

		if (numWorkers > 32) {
			numWorkers = 32;
		}

		System.out.println("Sequential:");
		Universe sequential = new Universe(numBodies, radius, steps, gui, seed, debug);
		sequential.runSequential();

		System.out.println("\nParallel:");
		Universe parallel = new Universe(numWorkers, numBodies, radius, steps, gui, seed, debug);
		parallel.runParallel();

	}

}
