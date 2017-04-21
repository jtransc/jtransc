package testservice.test;

import testservice.ITestServiceReferencedInServiceImpl;

import java.util.ServiceLoader;

public class TestServiceJs2 implements ITestService2 {
	@Override
	public String test() {
		String out = "TestServiceJs2";
		for (ITestServiceReferencedInServiceImpl impl : ServiceLoader.load(ITestServiceReferencedInServiceImpl.class)) {
			out += impl.value();
		}

		return out;
	}
}
