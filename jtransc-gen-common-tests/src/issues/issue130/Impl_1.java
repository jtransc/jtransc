package issues.issue130;

public class Impl_1 extends Impl_0 {
	public int a = 1;

	// https://docs.oracle.com/javase/tutorial/java/IandI/super.html
	public void test1() {
		super.printMe("OK:" + super.a + ":" + this.a);
	}

	@Override
	protected void printMe(String s) {
		super.printMe("FAIL:" + super.a + ":" + this.a);
	}
}
