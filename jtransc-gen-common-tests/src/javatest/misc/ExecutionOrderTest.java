package javatest.misc;

public class ExecutionOrderTest {
	static public void main(String[] args) {
		new ExecutionOrderTest().mytest();
	}

	private void mytest() {
		Reader r = new Reader();
		executionOrderTest(r, 10);
		executionOrderTest(r, 11);
	}

	// UndeterministicParameterEvaluationFeature should help to fix this in targets not supporting it
	// like C++ at the cost of being more verbose and having more locals
	private void executionOrderTest(Reader r, int version) {
		System.out.println("MiscTest.executionOrderTest:");
		System.out.println(1234554321);
		System.out.println(new TextFieldInfo(
			r.i16(),
			r.str(),
			r.str(),
			new Rectangle(r.i32(), r.i32(), r.i32(), r.i32()),
			r.str(),
			r.i32(),
			r.i32(),
			r.bool(),
			r.bool(),
			setMultiline(r.bool()),
			(version >= 11) ? true : false,
			(version >= 11) ? r.bool() : multiline,
			(version >= 11) ? r.f32() : 0.0,
			(version >= 11) ? r.f32() : 0.0,
			(version >= 11) ? r.f32() : 0.0,
			(version >= 11) ? r.f32() : 0.0,
			r.str(),
			false
		));
		System.out.println(r.index);
	}



	private boolean multiline = false;

	private boolean setMultiline(boolean input) {
		this.multiline = input;
		return input;
	}

	static class Reader {
		int index = 0;

		private short i16() {
			return (short) index++;
		}

		private String str() {
			return "str" + index++;
		}

		private boolean bool() {
			return (index++ % 2) != 0;
		}

		private int i32() {
			return index++;
		}

		private float f32() {
			return index++;
		}
	}

	static class TextFieldInfo {
		short i;
		String str;
		String str1;
		Rectangle rectangle;
		String str2;
		int i1;
		int i2;
		boolean bool;
		boolean bool1;
		boolean b;
		boolean b1;
		boolean b2;
		double v;
		double v1;
		double v2;
		double v3;
		String str3;
		boolean b3;

		public TextFieldInfo(short i, String str, String str1, Rectangle rectangle, String str2, int i1, int i2, boolean bool, boolean bool1, boolean b, boolean b1, boolean b2, double v, double v1, double v2, double v3, String str3, boolean b3) {
			this.i = i;
			this.str = str;
			this.str1 = str1;
			this.rectangle = rectangle;
			this.str2 = str2;
			this.i1 = i1;
			this.i2 = i2;
			this.bool = bool;
			this.bool1 = bool1;
			this.b = b;
			this.b1 = b1;
			this.b2 = b2;
			this.v = v;
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;
			this.str3 = str3;
			this.b3 = b3;
		}

		@Override
		public String toString() {
			return "TextFieldInfo{" +
				"i=" + i +
				", str='" + str + '\'' +
				", str1='" + str1 + '\'' +
				", rectangle=" + rectangle +
				", str2='" + str2 + '\'' +
				", i1=" + i1 +
				", i2=" + i2 +
				", bool=" + bool +
				", bool1=" + bool1 +
				", b=" + b +
				", b1=" + b1 +
				", b2=" + b2 +
				", v=" + v +
				", v1=" + v1 +
				", v2=" + v2 +
				", v3=" + v3 +
				", str3='" + str3 + '\'' +
				", b3=" + b3 +
				'}';
		}
	}

	static class Rectangle {
		public int x, y, width, height;

		public Rectangle(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString() {
			return "Rectangle{" +
				"x=" + x +
				", y=" + y +
				", width=" + width +
				", height=" + height +
				'}';
		}
	}
}
