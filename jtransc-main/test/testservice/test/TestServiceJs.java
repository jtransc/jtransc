package testservice.test;

import testservice.ITestServiceReferencedInServiceImpl;

import java.util.ServiceLoader;

public class TestServiceJs implements ITestService2 {
	@Override
	public String test() {
		String out = "TestServiceJs";
		for (ITestServiceReferencedInServiceImpl impl : ServiceLoader.load(ITestServiceReferencedInServiceImpl.class)) {
			out += impl.value();
		}

		return out;
	}
}
