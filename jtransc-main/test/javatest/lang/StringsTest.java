package javatest.lang;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mike on 4/11/15.
 */
public class StringsTest {

    public static void main(String[] args) throws Throwable {
        basicConcatTest();
		System.out.println(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z").format(new Date(0, 1, 2, 3, 4, 5)));
    }

    private static void basicConcatTest() {
        System.out.println("HELLO" + " " +"WORLD!");
    }

}