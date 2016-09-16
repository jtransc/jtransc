package jtransc.rt.test;

import com.jtransc.io.JTranscConsole;

public class MultidimensionalArrayTest {
	static public void main(String[] args) {
		test1();
		test2();
		test3();
		test4();
		test5();
		test6();
	}

	static private void test1() {
		JTranscConsole.log("MultidimensionalArrayTest.test1:");
		int sum = 0;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 4; x++) {
				sum += x * y * y;
			}
		}
		JTranscConsole.log(sum);
	}

	static private void test2() {
		JTranscConsole.log("MultidimensionalArrayTest.test2:");
		int[][] ints = new int[3][4];
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 4; x++) {
				ints[y][x] = x * y * y;
			}
		}
		JTranscConsole.log(ints.length);
		JTranscConsole.log(ints[0].length);
		JTranscConsole.log(ints[2][3]);
	}

	static private void test3() {
		JTranscConsole.log("MultidimensionalArrayTest.test3:");
		int[][][] ints = new int[3][4][5];
		for (int z = 0; z < 3; z++) {
			for (int y = 0; y < 4; y++) {
				for (int x = 0; x < 5; x++) {
					ints[z][y][x] = x * y * y * z * z * z;
				}
			}
		}
		JTranscConsole.log(ints.length);
		JTranscConsole.log(ints[0].length);
		JTranscConsole.log(ints[0][0].length);
		JTranscConsole.log(ints[2][3][4]);
	}

	static private void test4() {
		JTranscConsole.log("MultidimensionalArrayTest.test4:");
		int width = 3;
		int[] ints = new int[width];
		ints[1] = 1;
		String out = "";
		for (int x = 0; x < width; x++) {
			out += ints[x];
		}
		JTranscConsole.log(out);
	}

	static private void test5() {
		JTranscConsole.log("MultidimensionalArrayTest.test5:");
		int width = 3, height = 4;
		int[][] ints = new int[width][height];
		ints[1][2] = 1;
		for (int y = 0; y < height; y++) {
			String out = "";
			for (int x = 0; x < width; x++) {
				out += ints[x][y];
			}
			JTranscConsole.log(out);
		}
	}

	static private void test6() {
		JTranscConsole.log("MultidimensionalArrayTest.test6:");
		int width = 2, height = 3;
		int[][] ints = new int[width][height];
		ints[0][1] = 1;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				JTranscConsole.log(ints[x][y]);
			}
		}
	}
}
