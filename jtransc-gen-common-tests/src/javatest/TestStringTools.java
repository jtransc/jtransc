package javatest;

public class TestStringTools {
	static public String escape(String str) {
		StringBuilder out = new StringBuilder();
		for (int n = 0; n < str.length(); n++) {
			char c = str.charAt(n);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
				out.append(c);
			} else {
				switch (c) {
					case ' ':
					case '"':
					case '\'':
					case '#':
					case '=':
					case '&':
					case ',':
					case '.':
					case '+':
					case '-':
					case '*':
					case '/':
					case '[':
					case ']':
					case '{':
					case '}':
					case '<':
					case '>':
						out.append(c);
						break;
					default:
						out.append(String.format("\\u%04x", (int)c));
						break;
				}

			}
		}
		return out.toString();
	}
}
