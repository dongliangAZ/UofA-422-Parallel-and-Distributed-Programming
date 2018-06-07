

public class Body {

	private int id = 0;
	private double mass;// mass
	private double radius;// radius

	private Point position;// positions

	private Point force;// Forces

	private Point velocity;// Velocities

	public Body(double mass, double radius, Point position, Point force, Point velocity) {
		this.mass = mass;
		this.radius = radius;
		this.position = position;
		this.force = force;
		this.velocity = velocity;
	}

	public Body(double mass, double radius, double xPos, double yPos, double xForce, double yForce, double xVel,
			double yVel) {
		this.mass = mass;
		this.radius = radius;
		this.position = new Point(xPos, yPos);
		this.force = new Point(xForce, yForce);
		this.velocity = new Point(xVel, yVel);
	}

	public double getMass() {
		return mass;
	}

	public Point getPosition() {
		return position;
	}

	public double getXPos() {
		return this.position.getX();
	}

	public double getYPos() {
		return this.position.getY();
	}

	public void setPosition(Point position) {
		this.position = position;
	}

	public void setXPos(double x) {
		this.position.setX(x);
	}

	public void setYPos(double y) {
		this.position.setY(y);
	}

	public Point getForce() {
		return force;
	}

	public double getXForce() {
		return this.force.getX();
	}

	public double getYForce() {
		return this.force.getY();
	}

	public void setForce(Point force) {
		this.force = force;
	}

	public void setXForce(double x) {
		this.force.setX(x);
	}

	public void setYForce(double y) {
		this.force.setY(y);
	}

	public Point getVelocity() {
		return velocity;
	}

	public double getXVel() {
		return this.velocity.getX();
	}

	public double getYVel() {
		return this.velocity.getY();
	}

	public void setVelocity(Point velocity) {
		this.velocity = velocity;
	}

	public void setXVel(double x) {
		this.velocity.setX(x);
	}

	public void setYVel(double y) {
		this.velocity.setY(y);
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void auto(double deltaTime) {
		double deltaVX,deltaVY,deltaPX,deltaPY;
		
		deltaVX = (this.force.getX()/(this.mass))*deltaTime;
		deltaVY = (this.force.getY()/(this.mass))*deltaTime;
		deltaPX = (this.velocity.getX()+deltaVX/2)*deltaTime;
		deltaPY = (this.velocity.getY()+deltaVY/2)*deltaTime;
		
		this.velocity.setX(this.velocity.getX()+deltaVX);
		this.velocity.setY(this.velocity.getY()+deltaVY);
		this.position.setX(this.position.getX()+deltaPX);
		this.position.setY(this.position.getY()+deltaPY);
		this.force.setX(0.0);
		this.force.setY(0.0);

	}

	public synchronized void collision(Body p) {
		 
		
	}

}
