package java.text;

import com.jtransc.util.JTranscStringReader;

import java.util.*;

public class SimpleDateFormat extends DateFormat {
	private final List<String> patternChunks;
	private final DateFormatSymbols formatSymbols;

	public SimpleDateFormat() {
		this("", Locale.getDefault());
	}

	public SimpleDateFormat(String pattern) {
		this(pattern, Locale.getDefault());
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
	@SuppressWarnings("deprecation")
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
					value = String.valueOf(date.getMonth() + 1);
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
	@SuppressWarnings("deprecation")
	public Date parse(String source, ParsePosition pos) {
		int[] fields = new int[Calendar.FIELD_COUNT];

		JTranscStringReader r = new JTranscStringReader(source);
		for (String p : patternChunks) {
			int field = -1;
			switch (p.charAt(0)) {
				case 'Y':
					field = Calendar.YEAR;
					break;
				case 'y':
					field = Calendar.YEAR;
					break;
				case 'M':
					field = Calendar.MONTH;
					break;
				case 'd':
					field = Calendar.DAY_OF_MONTH;
					break;
				case 'H':
					field = Calendar.HOUR;
					break;
				case 'm':
					field = Calendar.MINUTE;
					break;
				case 's':
					field = Calendar.SECOND;
					break;
				case 'z':
					field = Calendar.ZONE_OFFSET;
					break;
				default:
					break;
			}
			String readed = r.read(p.length());
			if (field >= 0) {
				try {
					fields[field] = Integer.valueOf(readed);
				} catch (NumberFormatException nfe) {
				}
			}
		}
		pos.setIndex(r.offset);
		return new Date(fields[Calendar.YEAR] - 1900, fields[Calendar.MONTH] - 1, fields[Calendar.DAY_OF_MONTH], fields[Calendar.HOUR], fields[Calendar.MINUTE], fields[Calendar.SECOND]);
	}
}
