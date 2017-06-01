package issues.issue158;

public class Issue158 {
	static public void main(String[] args) throws Throwable {
		Impl_1.class.getMethod("getF");
		System.out.println("OK");
	}
}