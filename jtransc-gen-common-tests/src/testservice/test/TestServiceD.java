package testservice.test;

import testservice.ITestService;

public class TestServiceD implements ITestService2 {
	@Override
	public String test() {
		return "TestServiceD";
	}
}
