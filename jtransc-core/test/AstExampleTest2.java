public class AstExampleTest2 {
	int b;

	public void test(int a) {
		int result = (this.b == a) ? 10 : 20;
		System.out.println(result);
	}
}
