package java.text;

import java.util.Locale;

public class MessageFormat extends Format {
    public MessageFormat(String pattern) {
        this.locale = Locale.getDefault(Locale.Category.FORMAT);
        applyPattern(pattern);
    }

    public MessageFormat(String pattern, Locale locale) {
        this.locale = locale;
        applyPattern(pattern);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    @SuppressWarnings("fallthrough") // fallthrough in switch is expected, suppress it
    native public void applyPattern(String pattern);

    native public String toPattern();

    native public void setFormatsByArgumentIndex(Format[] newFormats);

    native public void setFormats(Format[] newFormats);

    native public void setFormatByArgumentIndex(int argumentIndex, Format newFormat);

    native public void setFormat(int formatElementIndex, Format newFormat);

    native public Format[] getFormatsByArgumentIndex();

    native public Format[] getFormats();

    native public final StringBuffer format(Object[] arguments, StringBuffer result, FieldPosition pos);

    public static String format(String pattern, Object... arguments) {
        return new MessageFormat(pattern).format(arguments);
    }

    native public final StringBuffer format(Object arguments, StringBuffer result, FieldPosition pos);

    native public AttributedCharacterIterator formatToCharacterIterator(Object arguments);

    native public Object[] parse(String source, ParsePosition pos);

    native public Object[] parse(String source) throws ParseException;

    public Object parseObject(String source, ParsePosition pos) {
        return parse(source, pos);
    }

    private Locale locale;
    private String pattern = "";
}

