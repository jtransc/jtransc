package jtransc.game.math;

public class Math2 {
	static public int clamp(int v, int min, int max) {
		return Math.min(Math.max(v, min), max);
	}

	static public double clamp(double v, double min, double max) {
		return Math.min(Math.max(v, min), max);
	}
}
