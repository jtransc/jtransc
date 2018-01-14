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

		System.out.println("[g]");
		System.out.println(getObj1().method(getRes1(), getRes2(), getRes3()));
		System.out.println(getOut());

		System.out.println("[h]");
		System.out.println(getObj1().chain(getRes1(), getRes2()).chain(getRes3()).chain(getRes4()).method(getRes1(), getRes2(), getRes3()));
		System.out.println(getOut());
	}

	private String[] demo = new String[2];

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

	private String getRes4() {
		out.append('4');
		return "4";
	}

	private Obj1 getObj1() {
		out.append('o');
		return new Obj1();
	}

	private class Obj1 {
		public String method(String a, String b, String c) {
			out.append('m');
			return ":" + a + ":" + b + ":" + c;
		}

		public Obj1 chain(String a, String b) {
			out.append('c');
			return this;
		}

		public Obj1 chain(String a) {
			out.append("c2");
			return this;
		}
	}
}
