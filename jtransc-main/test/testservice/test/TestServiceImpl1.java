package testservice.test;

import testservice.ITestService;

public class TestServiceImpl1 implements ITestService2 {
	public String ss = "ss";

	@Override
	public String test() {
		return "TestServiceImpl1.test:" + ss;
	}
}
