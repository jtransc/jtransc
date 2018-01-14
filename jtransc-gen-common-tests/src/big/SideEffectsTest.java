package big;

public class SideEffectsTest {
	static public void main(String[] args) {
		new SideEffectsTest().testSideEffects();
	}

	StringBuilder out = new StringBuilder();

	private String getOut() {
		String o = out.toString();
		out.setLength(0);
		return o;
	}

	private void testSideEffects() {
		System.out.println("SideEffectsTest:");

		System.out.println("[a]");
		getArray()[getIndex()] = getRes();
		System.out.println(getOut());

		System.out.println("[b]");
		System.out.println(getArray()[getIndex()]);
		System.out.println(getOut());

		System.out.println("[c]");
		System.out.println(getRes1() + getRes2() + getRes3());
		System.out.println(getOut());

		System.out.println("[d]");
		System.out.println(-(getInt1() - getInt2()) * getInt3());
		System.out.println(getOut());

		System.out.println("[e]");
		System.out.println(getRes1() + getArray()[getIndex()] + getRes2());
		System.out.println(getOut());

		System.out.println("[f]");
		System.out.println("" + getInt1() + call(call(getRes1(), getRes2()), getRes3()) + getInt2() + call(call(getRes1(), getRes2()), getRes3()) + getInt3());
		System.out.println(getOut());
	}

	String[] demo = new String[2];

	private String call(String a, String b) {
		out.append('C');
		return a + b;
	}

	private String[] getArray() {
		out.append('A');
		return demo;
	}

	private int getIndex() {
		out.append('B');
		return 0;
	}

	private int getInt1() {
		out.append('1');
		return 1;
	}

	private int getInt2() {
		out.append('2');
		return 2;
	}

	private int getInt3() {
		out.append('3');
		return 3;
	}

	private String getRes() {
		out.append('C');
		return "Z";
	}

	private String getRes1() {
		out.append('1');
		return "1";
	}

	private String getRes2() {
		out.append('2');
		return "2";
	}

	private String getRes3() {
		out.append('3');
		return "3";
	}
}
