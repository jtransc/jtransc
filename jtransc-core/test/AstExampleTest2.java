public class AstExampleTest2 {
	/*
	boolean test = true;

	private boolean isLetter(char c) {
		return c >= 'a' && c <= 'z';
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	*/

	//static private boolean isLetterOrDigit(char c) {
	//	return Character.isLetter(c) || Character.isDigit(c);
	//}
//
	//public boolean isDirectory() {
	//	//if (isInvalid()) return false;
	//	//return ((fs.getBooleanAttributes(this) & FileSystem.BA_DIRECTORY) != 0);
	//	return false;
	//}

	public void demo() {
		int[] items = new int[4];
		int a = 10;
		System.out.println(a >= 3);
		//System.out.println(items.length >= 3);
		//System.out.println(items[1] != 0); // .contains("test")
		//System.out.println(items[2] != 0); // .contains("main")
	}

	//public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
	//	return (prefix != null) ? new File(prefix) : null;
	//	//while (true) {
	//	//	System.out.println("test");
	//	//}
	//	/*
	//	if (prefix.length() < 3) throw new IllegalArgumentException("Prefix string too short");
	//	if (suffix == null) suffix = ".tmp";
//
	//	File tmpdir = (directory != null) ? directory : null;
	//	File f;
	//	do {
	//		f = new File("demo");
	//	} while (f.isFile());
//
	//	if (!f.isFile()) throw new IOException("Unable to create temporary file");
//
	//	return f;
	//	*/
	//}

	/*
	public static char forDigit(int digit, int radix) {
		if (digit >= 0 && digit <= 9) return (char) ('0' + (digit - 0));
		//if (digit >= 10 && digit <= 35) return (char) ('a' + (digit - 10));
		return '\0';
	}
	*/

	/*
	//private void test() { int[] a = new int[] { 1, 2, 3, 4, c };}
	private void test2() {
		StringBuilder sb = new StringBuilder();
		sb.append("A");
		if (((sb.length() * 2 + 2) % 2) == 0) {
			sb.append("B");
		} else {
			sb.append("C");
		}
		sb.append("D");
	}
	*/
}
