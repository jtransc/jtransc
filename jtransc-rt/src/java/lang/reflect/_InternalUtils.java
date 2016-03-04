/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang.reflect;

import jtransc.annotation.JTranscInvisible;

import java.util.ArrayList;

@JTranscInvisible
class _InternalUtils {
	static Class<?> Class_forName0(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			System.err.println("Can't find class '" + className + "'");
			return null;
		}
	}

	static MethodTypeImpl parseMethodType(String str, Type owner) {
		return (MethodTypeImpl) parseType(new MStringReader(str), owner);
	}

	static Type parseType(String str, Type owner) {
		return parseType(new MStringReader(str), owner);
	}

	static Type parseType(MStringReader sr, Type owner) {
		char c = sr.read();
		switch (c) {
			case '(':
				Type[] args = parseTypes(sr, owner);
				sr.expect(')');
				Type retval = parseType(sr, owner);
				return new MethodTypeImpl(args, retval);
			case '+': // Don't know the meaning!
				return parseType(sr, owner);
			case 'V':
				return Void.TYPE;
			case 'Z':
				return Boolean.TYPE;
			case 'B':
				return Byte.TYPE;
			case 'C':
				return Character.TYPE;
			case 'S':
				return Short.TYPE;
			case 'D':
				return Double.TYPE;
			case 'F':
				return Float.TYPE;
			case 'I':
				return Integer.TYPE;
			case 'J':
				return Long.TYPE;
			case '[':
				return new ArrayType(parseType(sr, owner));
			case 'L':
				int startOffset = sr.offset;
				boolean generic = false;
				while (true) {
					char cn = sr.read();
					if (cn == ';' || cn == '<') {
						if (cn == '<') generic = true;
						break;
					}
				}
				int endOffset = sr.offset - 1;

				String fqname = sr.str.substring(startOffset, endOffset).replace('/', '.');

				Class<?> base = Class_forName0(fqname);
				if (generic) {
					int genericStart = sr.offset;
					int diamondCount = 1;
					while (sr.hasMore()) {
						char cn = sr.read();
						if (cn == '<') diamondCount++;
						if (cn == '>') {
							diamondCount--;
							if (diamondCount <= 0) break;
						}
					}
					int genericEnd = sr.offset - 1;
					sr.expect(';');
					String genericParamsStr = sr.str.substring(genericStart, genericEnd);
					//System.out.println("genericParamsStr: " + genericParamsStr);
					Type[] paramTypes = parseTypes(genericParamsStr, owner);
					for (Type pt : paramTypes) {
						if (pt == null) throw new RuntimeException("Can't find one or more classes in '" + genericParamsStr + "'");
					}
					return new ParameterizedTypeImpl(paramTypes, base, owner);
				}
				return base;
		}
		throw new Error("Can't parse type '" + c + "'");
	}

	static Type[] parseTypes(String str, Type owner) {
		return parseTypes(new MStringReader(str, 0), owner);
	}

	static Type[] parseTypes(MStringReader sr, Type owner) {
		ArrayList<Type> types = new ArrayList<Type>();
		while (sr.hasMore()) {
			if (sr.peek() == ')') break;
			types.add(parseType(sr, owner));
		}
		return types.toArray(new Type[types.size()]);
	}

	@JTranscInvisible
	static private class MStringReader {
		public final String str;
		public final int length;
		public int offset;

		public MStringReader(String str) {
			this(str, 0);
		}

		public MStringReader(String str, int offset) {
			this.str = str;
			this.length = str.length();
			this.offset = offset;
		}

		public boolean hasMore() {
			return offset < length;
		}

		public char peek() {
			if (!hasMore()) throw new Error("Can't read more");
			return this.str.charAt(offset);
		}

		public void skip() {
			skip(1);
		}

		public void skip(int count) {
			offset += count;
		}

		public void expect(char c) {
			if (read() != c) throw new Error("Expected " + c);
		}

		public char read() {
			if (!hasMore()) throw new Error("Can't read more");
			char out = peek();
			skip();
			return out;
		}
	}

	static String getTypeName(Type type) {
		if (type instanceof Class<?>) {
			return ((Class<?>) type).getName();
		} else {
			return type.toString();
		}
	}
}

@JTranscInvisible
class ArrayType implements Type {
	public Type element;

	public ArrayType(Type element) {
		this.element = element;
	}
}

// @TODO: java.lang.invoke.MethodType
@JTranscInvisible
class MethodTypeImpl implements Type {
	public Type[] args;
	public Type rettype;

	public MethodTypeImpl(Type[] args, Type rettype) {
		this.args = args;
		this.rettype = rettype;
	}
}

@JTranscInvisible
class ParameterizedTypeImpl implements ParameterizedType {
	private Type[] actualTypeArguments;
	private Type rawType;
	private Type ownerType;

	public ParameterizedTypeImpl(Type[] actualTypeArguments, Type rawType, Type ownerType) {
		this.actualTypeArguments = actualTypeArguments;
		this.rawType = rawType;
		this.ownerType = ownerType;
	}

	@Override
	public Type[] getActualTypeArguments() {
		return actualTypeArguments;
	}

	@Override
	public Type getRawType() {
		return rawType;
	}

	@Override
	public Type getOwnerType() {
		return ownerType;
	}

	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(_InternalUtils.getTypeName(rawType));
		out.append('<');
		for (int n = 0; n < actualTypeArguments.length; n++) {
			if (n != 0) out.append(", ");
			out.append(_InternalUtils.getTypeName(actualTypeArguments[n]));
		}
		out.append('>');
		return out.toString();
	}
}
