package jtransc.rt.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest {
	// @TODO: Check that boxing works fine!

	static public void main(String[] args) throws Throwable {
		final A a = (A)Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{A.class}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				System.out.println(proxy != null);
				System.out.println(method.getName());
				System.out.println(args.length);
				System.out.println(args[0]);
				return 10;
			}
		});
		System.out.println(a.a(20));
		System.out.println(a.b(30));
	}

	interface A {
		int a(int arg);
		int b(int arg);
	}
}
