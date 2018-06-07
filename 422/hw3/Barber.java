/*
 *Student: Dong Liang
 *Description: A java program to perform the sleeping barber
 *		problem.
 */
public class Barber {
	private static int customersN;
	private static int haircutT;
	private static int arrivalT;
	private static int worker = 0;
	private static int chair = 0;
	private static int open = 0;

	private static barber barb;

	private static Object doorOpen = new Object();
	private static Object cunstomerCut = new Object();
	private static Object barberFree = new Object();
	private static Object chairFree = new Object();

	public static void main(String[] args) {
		arguments(args);
		barb = new barber();
		barb.start();

		for (int i = 0; i < customersN; i++) {
			Customer customer = new Customer(i);
			customer.start();
		}
	}

	private static void arguments(String[] args) {
		if (args.length != 4) {
			System.err.println("java Barber <Customers> <time for hair cut> <arrivalInterval> <num of waitting chairs>");
			System.exit(1);
		}
		customersN = Integer.parseInt(args[0]);
		haircutT = Integer.parseInt(args[1]);
		arrivalT = Integer.parseInt(args[2]);
		chair = Integer.parseInt(args[3]);

	}

	private static class Customer extends Thread {

		int i;

		public Customer(int Id) {
			this.i = Id;
		}

		public void run() {
			try {
				Thread.sleep(i * arrivalT * 1000);			
				event(i);
			} catch (InterruptedException x) {
				x.printStackTrace();
			}
		}

	}


	private static void event(int i) throws InterruptedException {
		String str = convertS(i+1);
		if (chair == 0) {
			
			System.out.printf("%d" + str + " customer is coming to the barber's!\n", i + 1);
			System.out.printf("Since there is no waitting chair so the %d" + str + " customer just leave\n", i + 1);


			return;
			
			
		}else {
	
		System.out.printf("%d" + str + " customer is coming to the barber's!\n", i + 1);

		while (worker == 0) {
			synchronized (barberFree) {
				barberFree.wait();
			}
		}

		worker--;
		chair++;

		System.out.printf("%d" + str + " customer is getting a haircut!\n", i + 1);
		Thread.sleep(haircutT * 1000);
		System.out.printf("%d" + str + " customer is done with the haircut.\n", i + 1);

		synchronized (chairFree) {
			chairFree.notify();
		}

		while (open == 0) {
			synchronized (doorOpen) {
				doorOpen.wait();
			}
		}

		open--;
		synchronized (cunstomerCut) {
			cunstomerCut.notify();
		}

		System.out.printf("%d" + str + " customer just left.\n", i + 1);
	
	}
	}
	private static String convertS(int i) {
		if(i==1)
		return "st";
		else if(i==2)
		return "nd";
		else if(i==3)
		return "rd";
		else if(i>3)
		return "th";
		else
		return "unknown";
	}

	private static class barber extends Thread {
		public void run() {

			System.out.printf("The barber's is open! Coming %d customers today!\n", customersN);

			for (int i = 0; i < customersN; i++) {
				try {
					next();
					done();
				} catch (InterruptedException x) {
					x.printStackTrace();
				}
			}

			System.out.println("The barber's is closed!");
		}

	}

	private static void done() throws InterruptedException {
		open++;
		synchronized (doorOpen) {
			doorOpen.notify();
		}
		while (open > 0) {
			synchronized (cunstomerCut) {
				cunstomerCut.wait();
			}
		}
	}

	private static void next() throws InterruptedException {
		worker++;
		synchronized (barberFree) {
			barberFree.notify();
		}
		while (chair == 0) {
			synchronized (chairFree) {
				chairFree.wait();
			}
		}
	
		chair--;
	}

}
