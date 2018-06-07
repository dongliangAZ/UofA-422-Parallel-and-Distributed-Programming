

import javax.swing.JFrame;
import javax.swing.JPanel;

//import Universe;
//import Body;

public class GUI extends JFrame {

	private static final long serialVersionUID = 893673076270700856L;

	protected ControllPanel controllPanel;
	protected JPanel drawPanle;
	private Body[] bodies;
	private Planet[] planets;
	private int SIZE = 1000;
	private int TIME = 100;

	public GUI(Universe universe) {
		bodies = universe.getBodies();
		buildFrame();
		controllPanel = new ControllPanel(this);
	}

	private void buildFrame() {
		this.setTitle("N-Bodies and Collisions Simulator");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(SIZE, SIZE);
		this.setLocationRelativeTo(null);
		this.setResizable(false);

		drawPanle = new JPanel();
		drawPanle.setLayout(null);

		planets = new Planet[bodies.length];

		this.drawPlanets();

		this.add(drawPanle);

		this.setVisible(true);
	}

	public void updatePlanets(Body[] bodies) {
		this.bodies = bodies;
		if (TIME > 0) {
			try {
				Thread.sleep(TIME);
			} catch (InterruptedException e) {
				System.err.println("ERROR: can not wait");
			}
		} else {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				System.err.println("ERROR: can not wait");
			}
		}
		drawPanle.removeAll();

		this.drawPlanets();

		drawPanle.repaint();

	}

	private void drawPlanets() {
		double x = 0;
		double y = 0;

		for (int i = 0; i < bodies.length; i++) {
			x = bodies[i].getXPos() + SIZE / 2;
			y = bodies[i].getYPos() + SIZE / 2;
			planets[i] = new Planet(bodies[i].getRadius());
			planets[i].setLocation((int) x, (int) y);
			planets[i].setSize(planets[i].getPreferredSize());
			drawPanle.add(planets[i]);
		}
		drawPanle.repaint();
	}

	public void setTime(int TIME) {
		this.TIME = TIME;
	}

}
