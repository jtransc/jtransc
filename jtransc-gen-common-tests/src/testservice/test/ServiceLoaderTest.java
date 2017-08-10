package testservice.test;

import java.util.ServiceLoader;

public class ServiceLoaderTest {
	static public void main(String[] args) {
		for (ITestService2 testService : ServiceLoader.load(ITestService2.class)) {
			System.out.println(testService.test());
		}
	}
}
