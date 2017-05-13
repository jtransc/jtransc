package javatest.lang;

import java.util.Enumeration;

public class PropertiesTest {
	public static void main(String[] args) {
		System.out.println("PropertiesTest.main:");
		Enumeration<Object> elements = System.getProperties().elements();
		while (elements.hasMoreElements()) {
			Object item = elements.nextElement();
			//System.out.println(item);
		}
	}
}
