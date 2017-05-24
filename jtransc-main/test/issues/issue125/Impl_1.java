package issues.issue125;

public class Impl_1 extends Impl_0 {

	public void test1() {
		super.printMe("OK");
	}

	@Override
	public void printMe(String s) {
		super.printMe("FAIL");
	}
}
