package java.text;

import java.io.Serializable;
import java.util.Locale;

public class DateFormatSymbols implements Serializable, Cloneable {
	public DateFormatSymbols() {

	}

	public DateFormatSymbols(Locale locale) {

	}

	native public static Locale[] getAvailableLocales();

	native public static final DateFormatSymbols getInstance();

	native public static final DateFormatSymbols getInstance(Locale locale);

	native public String[] getEras();

	native public void setEras(String[] newEras);

	native public String[] getMonths();

	native public void setMonths(String[] newMonths);

	native public String[] getShortMonths();

	native public void setShortMonths(String[] newShortMonths);

	native public String[] getWeekdays();

	native public void setWeekdays(String[] newWeekdays);

	native public String[] getShortWeekdays();

	native public void setShortWeekdays(String[] newShortWeekdays);

	native public String[] getAmPmStrings();

	native public void setAmPmStrings(String[] newAmpms);

	native public String[][] getZoneStrings();

	native public void setZoneStrings(String[][] newZoneStrings);

	native public String getLocalPatternChars();

	native public void setLocalPatternChars(String newLocalPatternChars);

	native public Object clone();

	@Override
	native public int hashCode();

	native public boolean equals(Object obj);
}
