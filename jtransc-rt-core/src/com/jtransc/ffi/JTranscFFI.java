package com.jtransc.ffi;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMeta;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyList;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

public class JTranscFFI {
	static public <T> T loadLibrary(String name, Class<T> clazz) {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		if (JTranscSystem.usingJTransc()) {
			String kind = JTranscSystem.getRuntimeKind();
			switch (kind) {
				case "js":
					return NodeFFI_Library.loadLibrary(name, clazz);
				case "cpp":
					return loadCppLibrary(name, clazz);
				default:
					throw new RuntimeException("Unsupported target for ffi " + kind);
			}

		}
		throw new RuntimeException("Not running on jtransc! TODO: Use JNA instead");
	}

	@HaxeMethodBody("" +
		//"trace('loadCppLibrary[0]');\n" +
		"var instance = N.newInstance(Type.getClassName(p1._hxFfiClass));\n" +
		//"trace('loadCppLibrary[1]');\n" +
		//"Reflect.callMethod(instance, Reflect.field(instance, '_ffi__load'), [p0._str]);\n" +
		"cast(instance, HaxeFfiLibrary)._ffi__load(p0._str);\n" +
		//"trace('loadCppLibrary[2]');\n" +
		"return instance;\n"
	)
	static native private <T> T loadCppLibrary(String lib, Class<T> clazz);

	@HaxeAddMembers("public var lib:Dynamic;")
	static public class NodeFFI_Library {
		static private String getTypeString(Type type) {
			if (type == Boolean.TYPE) return "uint";
			if (type == Integer.TYPE) return "int";
			if (type == Long.TYPE) return "long";
			if (type == Void.TYPE) return "void";
			if (type == String.class) return "string";
			throw new RuntimeException("Don't know how to serialize " + type);
			/*
			ftmap->Set(Nan::New<String>("void").ToLocalChecked(), WrapPointer((char *)&ffi_type_void));
			ftmap->Set(Nan::New<String>("uint8").ToLocalChecked(), WrapPointer((char *)&ffi_type_uint8));
			ftmap->Set(Nan::New<String>("int8").ToLocalChecked(), WrapPointer((char *)&ffi_type_sint8));
			ftmap->Set(Nan::New<String>("uint16").ToLocalChecked(), WrapPointer((char *)&ffi_type_uint16));
			ftmap->Set(Nan::New<String>("int16").ToLocalChecked(), WrapPointer((char *)&ffi_type_sint16));
			ftmap->Set(Nan::New<String>("uint32").ToLocalChecked(), WrapPointer((char *)&ffi_type_uint32));
			ftmap->Set(Nan::New<String>("int32").ToLocalChecked(), WrapPointer((char *)&ffi_type_sint32));
			ftmap->Set(Nan::New<String>("uint64").ToLocalChecked(), WrapPointer((char *)&ffi_type_uint64));
			ftmap->Set(Nan::New<String>("int64").ToLocalChecked(), WrapPointer((char *)&ffi_type_sint64));
			ftmap->Set(Nan::New<String>("uchar").ToLocalChecked(), WrapPointer((char *)&ffi_type_uchar));
			ftmap->Set(Nan::New<String>("char").ToLocalChecked(), WrapPointer((char *)&ffi_type_schar));
			ftmap->Set(Nan::New<String>("ushort").ToLocalChecked(), WrapPointer((char *)&ffi_type_ushort));
			ftmap->Set(Nan::New<String>("short").ToLocalChecked(), WrapPointer((char *)&ffi_type_sshort));
			ftmap->Set(Nan::New<String>("uint").ToLocalChecked(), WrapPointer((char *)&ffi_type_uint));
			ftmap->Set(Nan::New<String>("int").ToLocalChecked(), WrapPointer((char *)&ffi_type_sint));
			ftmap->Set(Nan::New<String>("float").ToLocalChecked(), WrapPointer((char *)&ffi_type_float));
			ftmap->Set(Nan::New<String>("double").ToLocalChecked(), WrapPointer((char *)&ffi_type_double));
			ftmap->Set(Nan::New<String>("pointer").ToLocalChecked(), WrapPointer((char *)&ffi_type_pointer));
			// NOTE: "long" and "ulong" get handled in JS-land
			// Let libffi handle "long long"
			ftmap->Set(Nan::New<String>("ulonglong").ToLocalChecked(), WrapPointer((char *)&ffi_type_ulong));
			ftmap->Set(Nan::New<String>("longlong").ToLocalChecked(), WrapPointer((char *)&ffi_type_slong));

			target->Set(Nan::New<String>("FFI_TYPES").ToLocalChecked(), ftmap);
			 */
		}

		static public <T> T loadLibrary(String name, Class<T> clazz) {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			Method[] methods = clazz.getDeclaredMethods();
			Function[] functions = new Function[methods.length];

			for (int m = 0; m < methods.length; m++) {
				Method method = methods[m];
				Class<?>[] params = method.getParameterTypes();
				String[] paramsString = new String[params.length];
				for (int n = 0; n < params.length; n++) paramsString[n] = getTypeString(params[n]);
				functions[m] = new Function(method.getName(), getTypeString(method.getReturnType()), paramsString);
			}

			final NodeFFI_Library library = new NodeFFI_Library(name, functions);

			return (T) Proxy.newProxyInstance(classLoader, new Class[]{clazz}, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					//System.out.println("PROXY: " + proxy);
					//System.out.println("Method: " + method);
					//System.out.println("Args: " + args);
					Object result = library.invoke(method.getName(), args);
					if (method.getReturnType() == Boolean.TYPE) {
						return ((Integer) result) != 0;
					} else {
						return result;
					}
				}
			});
		}

		static public class Function {
			public final String name;
			public final String retval;
			public final String[] args;

			public Function(String name, String retval, String... args) {
				this.name = name;
				this.retval = retval;
				this.args = args;
			}
		}

		@HaxeMethodBodyList({
			@HaxeMethodBody(target = "js", value = "" +
				"var ffi:Dynamic = untyped __js__(\"require('ffi')\");\n" +
				"var obj:Dynamic = {};\n" +
				"for (item in p1.toArray()) {\n" +
				"  Reflect.setField(obj, item.name, [N.toNativeString(item.retval), N.toNativeStrArray(item.args)]);\n" +
				"}\n" +
				"this.lib = ffi.Library(p0._str, obj);\n"
			),
			@HaxeMethodBody(""),
		})
		public NodeFFI_Library(String name, Function[] functions) {
		}

		@HaxeMethodBodyList({
			@HaxeMethodBody(target = "js", value = "" +
				"var name = N.toNativeString(p0);\n" +
				"var args = N.toNativeUnboxedArray(p1);\n" +
				"return N.box(Reflect.callMethod(this.lib, Reflect.field(this.lib, name), args));"
			),
			@HaxeMethodBody("return null;"),
		})
		public native Object invoke(String name, Object... args);
	}

	static public class Loader {
		@HaxeMeta("@:noStack")
		@HaxeMethodBody("return HaxeDynamicLoad.dlopen(p0._str);")
		@JTranscMethodBody(target = "cpp", value = "return dlopen(N::istr3(p0));")
		static native public long dlopen(String name);

		@HaxeMeta("@:noStack")
		@HaxeMethodBody("return HaxeDynamicLoad.dlsym(p0, p1._str);")
		@JTranscMethodBody(target = "cpp", value = "return dlsym(p0, N::istr3(p1));")
		static native public long dlsym(long handle, String name);

		@HaxeMeta("@:noStack")
		@HaxeMethodBody("return HaxeDynamicLoad.dlclose(p0);")
		@JTranscMethodBody(target = "cpp", value = "return dlclose(p0);")
		static native public int dlclose(long handle);
	}
}
