package com.jtransc.types;

public class AstTestExample {
	public void demo(int a) {
		/*
		int[][] ints = new int[3][4];
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 4; x++) {
				ints[y][x] = x * y * y;
			}
		}
		*/
		//int[] ints = new int[4];
		/*
		for (int x = a; x < b; x++) {
			System.out.println(x);
		}
		*/
		if (a < 3) {
			System.out.println("less than 3");
		} else {
			System.out.println("not less than 3");
		}
		//System.out.println(3 < 1);
	}
}