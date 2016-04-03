package com.jtransc.types;

public class AstTestExample {
	public void demo() {
		int[][] ints = new int[3][4];
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 4; x++) {
				ints[y][x] = x * y * y;
			}
		}
	}
}