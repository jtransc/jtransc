package issues.issue146;

public class Issue146 {
	static public void main(String[] args) throws Throwable {
		Impl_3 i3 = new Impl_3();
		Impl_1 i1 = i3;
		i1.init();
		i3.test1();
	}
}