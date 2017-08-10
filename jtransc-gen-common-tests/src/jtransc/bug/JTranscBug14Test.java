package jtransc.bug;

public class JTranscBug14Test {
	static public void main(String[] args) {
		Class<?> clazz = new ClassNotFoundException("MyClass").getClass();
		System.out.println(clazz.getCanonicalName());
		System.out.println(clazz.getName());
		System.out.println(clazz.getSimpleName());
	}
}
