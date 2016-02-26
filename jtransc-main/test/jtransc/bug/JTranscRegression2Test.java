package jtransc.bug;

public class JTranscRegression2Test {
	static public void main(String[] args) {
		Class<?> clazz = Object[].class;
		System.out.println(clazz.isArray());
		//System.out.println(clazz.getName());
		//System.out.println(clazz.getCanonicalName());
		//System.out.println(clazz.getSimpleName());
	}
}
