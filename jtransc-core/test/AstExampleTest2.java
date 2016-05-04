public class AstExampleTest2 {
	boolean test = true;

	//private void test() { int[] a = new int[] { 1, 2, 3, 4, c };}
	private void test2() {
		StringBuilder sb = new StringBuilder();
		sb.append("hello world!");
		if (((sb.length() * 2 + 2) % 2) == 0) {
			sb.append("hello world!");
		}
	}
}
