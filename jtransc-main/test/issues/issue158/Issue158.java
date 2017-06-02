package issues.issue158;

public class Issue158 {
	static public void main(String[] args) throws Throwable {
		Impl_0.class.getMethod("getF");
		System.out.println("OK1");
		Impl_1.class.getMethod("getF");
		System.out.println("OK2");
	}
}