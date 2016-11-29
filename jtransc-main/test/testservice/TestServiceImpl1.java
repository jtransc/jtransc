package testservice;

public class TestServiceImpl1 implements ITestService {
	public String ss = "ss";

	@Override
	public String test() {
		return "TestServiceImpl1.test:" + ss;
	}
}
