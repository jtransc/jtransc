package jtransc.ffi;

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JTranscFFI {
	static public <T> T loadLibrary(String name, Class<T> clazz) {
		//Method[] declaredMethods = clazz.getDeclaredMethods();

		return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{clazz}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				System.out.println("PROXY: " + proxy);
				System.out.println("Method: " + method);
				System.out.println("Args: " + args);
				return null;
			}
		});
	}

	@HaxeAddMembers("public var lib:Dynamic;")
	static public class NodeFFI_Library {
		static public class Function {
			public final String name;
			public final String retval;
			public final String[] args;

			public Function(String name, String retval, String ...args) {
				this.name = name;
				this.retval = retval;
				this.args = args;
			}
		}

		@HaxeMethodBody("" +
			"var ffi:Dynamic = untyped __js__(\"require('ffi')\");\n" +
			"var obj:Dynamic = {};\n" +
			"for (item in p1.toArray()) {\n" +
			"  Reflect.setField(obj, item.name, [HaxeNatives.toNativeString(item.retval), HaxeNatives.toNativeStrArray(item.args)]);\n" +
			"}\n" +
			"this.lib = ffi.Library(p0._str, obj);"
		)
		public NodeFFI_Library(String name, Function[] functions) {
		}

		@HaxeMethodBody("" +
			"var name = HaxeNatives.toNativeString(p0);\n" +
			"var args = HaxeNatives.toNativeUnboxedArray(p1);\n" +
			"return HaxeNatives.box(Reflect.callMethod(this.lib, Reflect.field(this.lib, name), args));"
		)
		public native Object invoke(String name, Object... args);
	}
}
