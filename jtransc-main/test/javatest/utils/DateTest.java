package javatest.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTest {
	public static void main(String[] args) throws Throwable {
		System.out.println("DateTest.main:");
		dateTest();
		dateTest2();
	}

	private static void dateTest() {
		//String formatString = "MM/dd/yyyy HH:mm:ss z";
		String formatString = "MM/dd/yyyy HH:mm:ss"; // time zone not reliable for tests
		SimpleDateFormat format = new SimpleDateFormat(formatString, Locale.US);
		Date date = new Date(0, 1, 2, 3, 4, 5);
		String formattedString = format.format(date);
		System.out.println(date.getMonth());
		System.out.println(date.getDate());
		System.out.println(date.getYear());
		System.out.println(date.getHours());
		System.out.println(date.getMinutes());
		System.out.println(date.getSeconds());
		System.out.println(formattedString);
		try {
			System.out.println(format.format(format.parse(formattedString)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private static void dateTest2() {
		System.out.println("dateTest2:");
		Date date = new Date(0L);
		date.setTime(1L);
		System.out.println(date.getTime());
	}
}
