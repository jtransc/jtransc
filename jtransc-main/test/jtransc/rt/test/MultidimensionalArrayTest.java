package jtransc.rt.test;

public class MultidimensionalArrayTest {
    static public void main(String[] args) {
        test2();
        test3();
    }

    static private void test2() {
        int[][] ints = new int[3][4];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 4; x++) {
                ints[y][x] = x * y * y;
            }
        }
        System.out.println(ints.length);
        System.out.println(ints[0].length);
        System.out.println(ints[2][3]);
    }

    static private void test3() {
        int[][][] ints = new int[3][4][5];
        for (int z = 0; z < 3; z++) {
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 5; x++) {
                    ints[z][y][x] = x * y * y * z * z * z;
                }
            }
        }
        System.out.println(ints.length);
        System.out.println(ints[0].length);
        System.out.println(ints[0][0].length);
        System.out.println(ints[2][3][4]);
    }
}
