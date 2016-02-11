package jtransc.game.math;

public interface Interpolable<T> {
	T interpolate(T that, double ratio);
}
