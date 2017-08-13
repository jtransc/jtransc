package javatest.time;

import java.time.Period;

public class PeriodTest {
	static public void main(String[] args) {
		System.out.println("PeriodTest.main:");
		System.out.println(Period.ofDays(0).toString());
		System.out.println(Period.ofDays(1).toString());
		System.out.println(Period.ofDays(1).plusDays(40).toString());
		System.out.println(Period.parse(Period.ofDays(1).toString()));
	}
}
