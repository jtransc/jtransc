package jtransc.bug;

public class JTranscRegression2Test {
	static public boolean a = false;
	static public boolean b = true;

	static public void main(String[] args) {
		Class<?> clazz = Object[].class;
		System.out.println(clazz.isArray());
		//System.out.println(clazz.getName());
		//System.out.println(clazz.getCanonicalName());
		//System.out.println(clazz.getSimpleName());
		boolean[] list = new boolean[2];
		list[0] = true;
		list[1] = true;
		System.out.println(list[0]);
		System.out.println(a);
		System.out.println(b);
	}
}
