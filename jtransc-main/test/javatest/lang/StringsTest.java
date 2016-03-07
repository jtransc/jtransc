package javatest.lang;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mike on 4/11/15.
 */
public class StringsTest {

    public static void main(String[] args) throws Throwable {
        basicConcatTest();
		dateTest();
    }

	private static void dateTest() {
		String formatString = "MM/dd/yyyy HH:mm:ss z";
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		String formattedString = format.format(new Date(0, 1, 2, 3, 4, 5));
		System.out.println(formattedString);
		try {
			System.out.println(format.format(format.parse(formattedString)));
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	private static void basicConcatTest() {
        System.out.println("HELLO" + " " +"WORLD!");
    }

}