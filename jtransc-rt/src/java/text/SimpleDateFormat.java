package java.text;

import jtransc.util.JTranscStringReader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleDateFormat extends DateFormat {
	private final List<String> patternChunks;
	private final DateFormatSymbols formatSymbols;

	public SimpleDateFormat() {
		this("", Locale.getDefault(Locale.Category.FORMAT));
	}

	public SimpleDateFormat(String pattern) {
		this(pattern, Locale.getDefault(Locale.Category.FORMAT));
	}

	public SimpleDateFormat(String pattern, Locale locale) {
		this(pattern, new DateFormatSymbols(locale));
	}

	public SimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
		this.patternChunks = compilePattern(pattern);
		this.formatSymbols = formatSymbols;
	}

	static private List<String> compilePattern(String pattern) {
		JTranscStringReader r = new JTranscStringReader(pattern);
		ArrayList<String> out = new ArrayList<>();
		while (!r.eof()) {
			String result = r.tryRead("YYYY", "yyyy", "MM", "mm", "dd", "HH", "ss", "hh", "M", "m", "s", "z");
			if (result != null) {
				out.add(result);
			} else {
				out.add(r.read(1));
			}
		}
		return out;
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		for (String p : patternChunks) {
			String value = null;
			switch (p.charAt(0)) {
				case 'Y':
					value = String.valueOf(date.getYear() + 1900);
					break;
				case 'y':
					value = String.valueOf(date.getYear() + 1900);
					break;
				case 'M':
					value = String.valueOf(date.getMonth());
					break;
				case 'd':
					value = String.valueOf(date.getDate());
					break;
				case 'H':
					value = String.valueOf(date.getHours());
					break;
				case 'm':
					value = String.valueOf(date.getMinutes());
					break;
				case 's':
					value = String.valueOf(date.getSeconds());
					break;
				case 'z':
					value = "CET";
					break;
				default:
					break;
			}
			if (value != null) {
				while (value.length() < p.length()) value = "0" + value;
				toAppendTo.append(value);
			} else {
				toAppendTo.append(p);
			}
		}

		//toAppendTo.append()
		return toAppendTo;
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		int year = 1900;
		int month = 1;
		int day = 1;
		int hour = 0;
		int min = 0;
		int sec = 0;
		JTranscStringReader r = new JTranscStringReader(source);
		for (String p : patternChunks) {
			switch (p) {
				case "YYYY":
					year = Integer.parseInt(r.read(4));
					break;
				default:
					String readed = r.read(p.length());
					break;
			}
		}
		return new Date(year, month, day, hour, min, sec);
	}
}
