public class AstExampleTest2 {
	int c = 10;
	//private void test() { int[] a = new int[] { 1, 2, 3, 4, c };}
	private int test2(Object param) {
		kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(param, "param");
		return c;
	}
}
