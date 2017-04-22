package jtransc.bug;

public class JTranscBug110 {
	static private String[] tokenImage = {
		"<EOF>",
		"\" \"",
		"\"\\r\"",
		"\"\\t\"",
		"\"+\"",
		"\"-\"",
		"\"*\"",
		"\"/\"",
		"\"\\n\"",
		"<INTEGER>",
		"<RATIONAL>",
		"<DIGIT>",
		"\"[\"",
		"\"]\"",
		"\"(\"",
		"\")\"",
	};

	static public void main(String[] args) {
		System.out.println("[1]");
		generateParseException("a", "b");
		System.out.println("[2]");
	}

	static private ParseException generateParseException(String token, String exptokseq) {
		return new ParseException(token, exptokseq, tokenImage);
	}

	static public class ParseException {
		public final String token;
		public final String exptokseq;
		public final String[] tokenImage;

		public ParseException(String token, String exptokseq, String[] tokenImage) {
			this.token = token;
			this.exptokseq = exptokseq;
			this.tokenImage = tokenImage;
		}
	}
}
