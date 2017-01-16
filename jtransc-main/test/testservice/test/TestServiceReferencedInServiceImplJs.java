package testservice.test;

import testservice.ITestServiceReferencedInServiceImpl;

public class TestServiceReferencedInServiceImplJs implements ITestServiceReferencedInServiceImpl {
	@Override
	public int value() {
		return 10;
	}
}
