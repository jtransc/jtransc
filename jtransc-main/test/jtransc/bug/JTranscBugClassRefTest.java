package jtransc.bug;

public class JTranscBugClassRefTest {
	static public void main(String[] args) {
		Class<?> clazz = MyClass.class;
		System.out.println(clazz.getName());
		System.out.println(clazz.getCanonicalName());
		System.out.println(clazz.getSimpleName());
		//System.out.println(clazz.getTypeName());
	}

	class MyClass {
	}
}
