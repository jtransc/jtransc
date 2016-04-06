package jtransc.bug;

public class JTranscBug12Test {
	static public void main(String[] args) {
		System.out.println("[1]");
		//Test0<Integer> ref = new ImplTest0();
		ImplTest1 test1 = new ImplTest0();
		test1.getTest();
		System.out.println("[2]");
	}

	static class ImplTest0 extends ImplTest1 {
		public TestSetter getTest() {
			return new Test0.Setter();
		}
	}

	static abstract class ImplTest1 extends ImplTest2 {
	}

	static abstract class ImplTest2 {
		abstract public TestSetter getTest();
	}

	interface TestSetter {
	}

	interface Test0 {
		class Setter implements TestSetter {
		}
	}
}
