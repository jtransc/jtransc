package jtransc.game.math;

public class Point {
	public double x;
	public double y;

	public Point() {
		this(0, 0);
	}

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point setTo(double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}
}
