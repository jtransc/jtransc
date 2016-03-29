package java.text;

public class MessageFormat extends Format {
	@Override
	native public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos);

	@Override
	native public Object parseObject(String source, ParsePosition pos);
}