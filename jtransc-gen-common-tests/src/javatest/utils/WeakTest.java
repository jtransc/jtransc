package javatest.utils;

import com.jtransc.io.JTranscConsole;

import java.lang.reflect.Array;
import java.util.Arrays;

@SuppressWarnings("ConstantConditions")
public class WeakTest {
	static public void main(String[] args) {
		JTranscConsole.log("WeakTest.main()");
		JTranscConsole.log(Arrays.toString(args));
		JTranscConsole.log(mysum());
		JTranscConsole.log(new B() instanceof A);
		JTranscConsole.log(new B() instanceof B);
		JTranscConsole.log(new B() instanceof Object);
		JTranscConsole.log(new A() instanceof A);
		JTranscConsole.log(new A() instanceof B);
		JTranscConsole.log(new A() instanceof Object);
		dump(new B());
		System.out.println(Integer.valueOf(10));
		System.out.println(Arrays.toString(new Object[] {Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(12)}));
		formatTest();

		System.out.println(new String[0] != null);
		System.out.println(new String[0].getClass());
		System.out.println(new String[0].getClass().getComponentType());

		Object out = Array.newInstance(new String[0].getClass().getComponentType(), 10);

		System.out.println(out != null);

		System.out.println(Arrays.toString((String[]) out));
	}

	static public void formatTest() {
		JTranscConsole.log("formatTest1:");
		String result = String.format("%d, Hello World This Is A Test %d", 10, 20);
		JTranscConsole.log("formatTest2:");
		System.out.println(result);
		JTranscConsole.log("formatTest3:");
	}

	static private void dump(A a) {
		JTranscConsole.log(a.v());
	}

	static private Point mysum() {
		//Point p1 = new Point(10, 20);
		//Point p2 = new Point(30, 40);
		//return Point.add(p1, p2);
		return Point.add(new Point(10, 20), new Point(30, 40));
	}

	static class A {
		public int v() {
			return 10;
		}
	}

	static class B extends A {
		public int v() {
			return 11;
		}
	}

	static class Point {
		public final int x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
			JTranscConsole.log("Point(" + x + "," + y + ")");
		}

		static public Point add(Point a, Point b) {
			return new Point(a.x + b.x, a.y + b.y);
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			JTranscConsole.log("~Point(" + x + "," + y + ")");
		}

		@Override
		public String toString() {
			return "Point(" + x + ", " + y + ")";
		}
	}
}
