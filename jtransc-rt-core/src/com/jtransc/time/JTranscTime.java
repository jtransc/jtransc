package com.jtransc.time;

import java.util.GregorianCalendar;

public class JTranscTime {
	static public final int PARTS = 10;

	//@JTranscMethodBody(target = "cpp", value = {
	//	"struct tm i = {0};",
	//	"i.tm_year  = p0 - 1900;",
	//	"i.tm_mon   = p1;",
	//	"i.tm_mday  = p2;",
	//	"i.tm_hour  = p3;",
	//	"i.tm_min   = p4;",
	//	"i.tm_sec   = p5;",
	//	"i.tm_isdst = -1;",
	//	"time_t result = (mktime(&i) * 1000L) + p6;",
	//	"printf(\"::mktime %lld\\n\", (int64_t)result);",
	//	"printf(\"%d,%d,%d,%d,%d,%d\\n\", p0, p1, p2, p3, p4, p5);",
	//	"return result;"
	//})
	static public long make(int fullyear, int month, int date, int hrs, int min, int sec, int milliseconds) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(fullyear, month, date, hrs, min, sec);
		return calendar.getTimeInMillis();
	}

	static public long parse(String str) {
		GregorianCalendar calendar = new GregorianCalendar();
		return 0L;
	}

	//@JTranscMethodBody(target = "cpp", value = {
	//	"time_t rawtime = p0 / 1000L;",
	//	"struct tm * i;",
	//	"JA_I *out = GET_OBJECT(JA_I, p1);",
	//	"i = ::localtime(&rawtime);",
	//	"printf(\"::localtime: %p %lld\\n\", i, (int64_t)rawtime);",
	//	"if (i == NULL) return;",
	//	"out->set(0, i->tm_year + 1900);",
	//	"out->set(1, i->tm_mon);",
	//	"out->set(2, i->tm_mday);",
	//	"out->set(3, i->tm_wday);",
	//	"out->set(4, i->tm_yday);",
	//	"out->set(5, i->tm_hour);",
	//	"out->set(6, i->tm_min);",
	//	"out->set(7, i->tm_sec);",
	//	"out->set(8, (int)(p0 % 1000L));",
	//	"out->set(9, i->tm_isdst);",
	//})
	static public void fillParts(long time, int[] parts) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(time);
		parts[0] = calendar.get(GregorianCalendar.YEAR);
		parts[1] = calendar.get(GregorianCalendar.MONTH);
		parts[2] = calendar.get(GregorianCalendar.DAY_OF_MONTH);
		parts[3] = calendar.get(GregorianCalendar.DAY_OF_WEEK);
		parts[4] = calendar.get(GregorianCalendar.DAY_OF_YEAR);
		parts[5] = calendar.get(GregorianCalendar.HOUR);
		parts[6] = calendar.get(GregorianCalendar.MINUTE);
		parts[7] = calendar.get(GregorianCalendar.SECOND);
		parts[8] = calendar.get(GregorianCalendar.MILLISECOND);
		parts[9] = 0;

		//for (int n = 0; n < parts.length; n++) parts[n] = 0;
	}

	public static int getFullYear(int[] parts) {
		return parts[0];
	}

	static public int getMonth(int[] parts) {
		return parts[1];
	}

	public static int getMonthDay(int[] parts) {
		return parts[2];
	}

	static public int getWeekDay(int[] parts) {
		return parts[3];
	}

	static public int getYearDay(int[] parts) {
		return parts[4];
	}

	public static int getHours(int[] parts) {
		return parts[5];
	}

	public static int getMinutes(int[] parts) {
		return parts[6];
	}

	public static int getSeconds(int[] parts) {
		return parts[7];
	}

	public static int getMilliseconds(int[] parts) {
		return parts[8];
	}

	public static int getDaylightTimeFlag(int[] parts) {
		return parts[9];
	}
}
