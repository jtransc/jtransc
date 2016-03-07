package java.util;

import java.io.Serializable;

abstract public class TimeZone implements Serializable, Cloneable {
	public TimeZone() {
	}

	public static final int SHORT = 0;
	public static final int LONG = 1;

	public abstract int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds);

	native public int getOffset(long date);

	native int getOffsets(long date, int[] offsets);

	abstract public void setRawOffset(int offsetMillis);

	public abstract int getRawOffset();

	native public String getID();

	native public void setID(String ID);

	native public final String getDisplayName();

	native public final String getDisplayName(Locale locale);

	native public final String getDisplayName(boolean daylight, int style);

	native public String getDisplayName(boolean daylight, int style, Locale locale);

	native public int getDSTSavings();

	public abstract boolean useDaylightTime();

	native public boolean observesDaylightTime();

	abstract public boolean inDaylightTime(Date date);

	native public static synchronized TimeZone getTimeZone(String ID);

	//native public static TimeZone getTimeZone(ZoneId zoneId);

	//native public ZoneId toZoneId();

	native public static synchronized String[] getAvailableIDs(int rawOffset);

	native public static synchronized String[] getAvailableIDs();

	native public static TimeZone getDefault();

	native public static void setDefault(TimeZone zone);

	native public boolean hasSameRules(TimeZone other);

	native public Object clone();

	native private static final TimeZone parseCustomTimeZone(String id);
}
