package jtransc.bug;

public class JTranscRegression1Test {
	static public void main(String[] args) {
		System.out.println(new MyConcrete().getValue());
	}

	static class MyConcrete extends MyAbstract {
		@Override
		public int getValue() {
			return 1;
		}
	}

	static abstract class MyAbstract implements MyInterface {
	}

	interface MyInterface {
		int getValue();
	}
}
