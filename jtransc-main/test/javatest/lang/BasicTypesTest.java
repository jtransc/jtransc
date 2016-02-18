package javatest.lang;

/**
 * Created by mike on 4/11/15.
 */
public class BasicTypesTest {

    public static void main(String[] args) throws Throwable {
        byteTests();
        charTests();
        shortTests();
        integerTests();
        longTests();
    }

    private static void byteTests() {
        byte one = 1;
        byte min = Byte.MIN_VALUE;
        System.out.println("Byte MIN_VALUE: " + min);
        byte max = Byte.MAX_VALUE;
        System.out.println("Byte MAX_VALUE: " + max);
        byte maxOverflow = (byte) (max + one);
        System.out.println("Byte MAX overflow: " + maxOverflow);
        byte minOverflow = (byte) (max - one);
        System.out.println("Byte MIN overflow: " + minOverflow);
        System.out.println("Byte MAX - MIN: " + (max - min));
    }

    private static void charTests() {
        char one = 1;
        char min = Character.MIN_VALUE;
        System.out.println("Char MIN_VALUE: " + min);
        char max = Character.MAX_VALUE;
        System.out.println("Char MAX_VALUE: " + max);
        char maxOverflow = (char) (max + one);
        System.out.println("Char MAX overflow: " + maxOverflow);
        char minOverflow = (char) (max - one);
        System.out.println("Char MIN overflow: " + minOverflow);
        System.out.println("Char MAX - MIN: " + (max - min));
    }

    private static void shortTests() {
        short one = 1;
        short min = Short.MIN_VALUE;
        System.out.println("Short MIN_VALUE: " + min);
        short max = Short.MAX_VALUE;
        System.out.println("Short MAX_VALUE: " + max);
        short maxOverflow = (short) (max + one);
        System.out.println("Short MAX overflow: " + maxOverflow);
        short minOverflow = (short) (max - one);
        System.out.println("Short MIN overflow: " + minOverflow);
        System.out.println("Short MAX - MIN: " + (max - min));

        short five = 5;
        short mult = (short) (five * five);
        System.out.println("Short 5 * 5: " + mult);

        short div = (short) (five / five);
        System.out.println("Short 5 / 5: " + div);

        short mod = (short) (five % 2);
        System.out.println("Short 5 % 2: " + mod);

        short shift = 256 >> 2;
        System.out.println("Short 256 >> 2: " + shift);

        short unshift = 256 << 2;
        System.out.println("Short 256 << 2: " + unshift);

    }

    private static void integerTests() {
        int one = 1;
        int min = Integer.MIN_VALUE;
        System.out.println("Int MIN_VALUE: " + min);
        int max = Integer.MAX_VALUE;
        System.out.println("Int MAX_VALUE: " + max);
        System.out.println("Int MAX overflow: " + (max + one));
        System.out.println("Int MIN overflow: " + (min - one));
        System.out.println("Int MAX - MIN: " + (max - min));

        int mult = 5 * 5;
        System.out.println("Int 5 * 5: " + mult);

        int div = 5 / 5;
        System.out.println("Int 5 / 5: " + div);

        int mod = 5 % 2;
        System.out.println("Int 5 % 2: " + mod);

        int shift = 256 >> 2;
        System.out.println("Int 256 >> 2: " + shift);

        int unshift = 256 << 2;
        System.out.println("Int 256 << 2: " + unshift);
    }

    private static void longTests() {
        long min = Long.MIN_VALUE;
        System.out.println("Long MIN_VALUE: " + min);
        long max = Long.MAX_VALUE;
        System.out.println("Long MAX_VALUE: " + max);
        System.out.println("Long MAX overflow: " + (max + 1));
        System.out.println("Long MIN overflow: " + (min - 1));
        System.out.println("Long MAX - MIN: " + (max - min));

        long five = 5;
        long mult = five * five;
        System.out.println("Long 5 * 5: " + mult);

        long div = five / five;
        System.out.println("Long 5 / 5: " + div);

        long mod = five % 2L;
        System.out.println("Long 5 % 2: " + mod);

        long shift = 256L >> 2L;
        System.out.println("Long 256 >> 2: " + shift);

        long unshift = 256L << 2L;
        System.out.println("Long 256 << 2: " + unshift);
    }

}