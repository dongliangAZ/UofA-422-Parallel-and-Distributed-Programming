

import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ControllPanel extends JFrame implements ChangeListener {

	private static final long serialVersionUID = -6318958439954232515L;

	private int width = 240, height = 100;
	private GUI gui;
	private JSlider speedController;

	private JPanel speedControllerPanel;
	private JPanel mainPanel;

	private int LOW = 160;
	private int HIGH = 0;
	private int MID = 80;
	private int interval = LOW / 4;

	public ControllPanel(GUI gui) {
		this.gui = gui;
		this.buildFrame();
	}

	private void buildFrame() {
		this.setTitle("Controll Panel");
		this.setLocation(800, 20);
		this.setSize(width, height);

		speedController = new JSlider(JSlider.HORIZONTAL, HIGH, LOW, MID);
		speedController.addChangeListener(this);
		speedController.setMajorTickSpacing(interval);
		speedController.setPaintTicks(true);

		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(HIGH, new JLabel("Slow"));
		labelTable.put(LOW, new JLabel("Fast"));

		speedController.setPaintLabels(true);

		speedControllerPanel = new JPanel();
		speedControllerPanel.add(speedController);

		mainPanel = new JPanel();
		mainPanel.add(speedControllerPanel);

		this.add(mainPanel);

		this.setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		JSlider source = (JSlider) e.getSource();

		if (!source.getValueIsAdjusting()) {
			int timeSpeed = (int) source.getValue();
			gui.setTime(LOW - timeSpeed);
		}

	}
}
