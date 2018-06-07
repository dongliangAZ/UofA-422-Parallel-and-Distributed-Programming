

public class Worker extends Thread {

	private int id;
	private Universe universe;

	public Worker(int id, Universe universe) {
		this.id = id;
		this.universe = universe;
	}

	@Override
	public void run() {
		for (int i = 0; i < universe.getSteps(); i++) {
			universe.calculateForces(this.id);
			barrier(0);

			universe.moveBodies(this.id);
			barrier(1);

			universe.detectCollisions(this.id);
			barrier(2);

			if (id == 0 && universe.isFlag()) {
				universe.getGui().updatePlanets(universe.getBodies());
			}
		}
	}

	private void barrier(int barrierIndex) {
		universe.aquireMutex();
		if (universe.getNumArrived() < universe.getNumWorkers()) {
			universe.setNumArrived(universe.getNumArrived() + 1);
			universe.releaseMutex();
			universe.acquireBarrier(barrierIndex);
		} else {
			universe.setNumArrived(1);
			universe.releaseBarriers(barrierIndex);
			universe.releaseMutex();
		}
	}

}
