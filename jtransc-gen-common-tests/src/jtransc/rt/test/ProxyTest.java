package jtransc.rt.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {
	// @TODO: Check that boxing works fine!

	static public void main(String[] args) throws Throwable {
		System.out.println("ProxyTest.main:");
		final A a = (A)Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{A.class}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				System.out.println("[A]");
				System.out.println(proxy != null);
				System.out.println((method != null) ? method.getName() : "null");
				System.out.println((args != null) ? args.length : "null");
				System.out.println((args != null) ? args[0] : "null");
				System.out.println("[B]");
				return 10;
			}
		});
		System.out.println("[C]");
		System.out.println(a != null);
		if (a != null) {
			System.out.println(a.a(20));
			System.out.println(a.b(30));
		}
		System.out.println("[D]");
	}

	interface A {
		int a(int arg);
		int b(int arg);
	}
}
