package issues.issue146;

public class Impl_3 extends Impl_2 {
	private boolean bodyIgnored = true;

	@Override
	public void init() {
		super.init();
		bodyIgnored = false;
	}

	public void test1() {
		if (bodyIgnored) {
			System.out.println("FAIL: body ignored");
			System.err.println("FAIL!");
		} else {
			System.out.println("OK");
		}
	}
}
