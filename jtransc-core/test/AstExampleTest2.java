public class AstExampleTest2 {
	static class Test {
		private long elements = 0;

		public Internal demo() {
			return new Internal();
		}

		class Internal {
			private void testEmptyStack() {
				elements = 0;
			}
		}
	}
}
