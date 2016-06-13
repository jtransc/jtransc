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

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;

public final class Array {
	private Array() {
	}

	@HaxeMethodBody("return new HaxeArrayBool(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_Z(p0);")
	native private static Object newBoolInstance(int length) throws NegativeArraySizeException;

	@HaxeMethodBody("return new HaxeArrayByte(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_B(p0);")
	native private static Object newByteInstance(int length) throws NegativeArraySizeException;

	@HaxeMethodBody("return new HaxeArrayChar(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_C(p0);")
	native private static Object newCharInstance(int length) throws NegativeArraySizeException;

	@HaxeMethodBody("return new HaxeArrayShort(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_S(p0);")
	native private static Object newShortInstance(int length) throws NegativeArraySizeException;

	@HaxeMethodBody("return new HaxeArrayInt(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_I(p0);")
	native private static Object newIntInstance(int length) throws NegativeArraySizeException;

	@HaxeMethodBody("return new HaxeArrayLong(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_J(p0);")
	native private static Object newLongInstance(int length) throws NegativeArraySizeException;

	@HaxeMethodBody("return new HaxeArrayFloat(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_F(p0);")
	native private static Object newFloatInstance(int length) throws NegativeArraySizeException;

	@HaxeMethodBody("return new HaxeArrayDouble(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_D(p0);")
	native private static Object newDoubleInstance(int length) throws NegativeArraySizeException;

	@HaxeMethodBody("return new HaxeArrayAny(p0, p1._str);")
	@JTranscMethodBody(target = "js", value = "return new JA_L(p0, N.istr(p1));")
	native private static Object newObjectInstance(int length, String desc) throws NegativeArraySizeException;

	public static Object newInstance(Class<?> type, int length) throws NegativeArraySizeException {
		if (type.isPrimitive()) {
			if (type == Boolean.TYPE) {
				return newBoolInstance(length);
			} else if (type == Byte.TYPE) {
				return newByteInstance(length);
			} else if (type == Short.TYPE) {
				return newShortInstance(length);
			} else if (type == Character.TYPE) {
				return newCharInstance(length);
			} else if (type == Integer.TYPE) {
				return newIntInstance(length);
			} else if (type == Long.TYPE) {
				return newLongInstance(length);
			} else if (type == Float.TYPE) {
				return newFloatInstance(length);
			} else if (type == Double.TYPE) {
				return newDoubleInstance(length);
			} else if (type == Void.TYPE) {
			}
		} else {
			return newObjectInstance(length, "[" + type.getName());
		}
		throw new RuntimeException("Invalid Array.newInstance with " + type);
	}

	public static Object newInstance(Class<?> componentType, int... dimensions) throws IllegalArgumentException, NegativeArraySizeException {
		if (dimensions.length == 1) {
			return newInstance(componentType, dimensions[0]);
		} else {
			throw new RuntimeException("Not implemented dynamically creating multidimensional arrays!");
		}
	}

    @HaxeMethodBody("return cast(p0, HaxeArrayBase).length;")
	@JTranscMethodBody(target = "js", value = "return p0.length;")
	native public static int getLength(Object array) throws IllegalArgumentException;


	@HaxeMethodBody("return cast(p0, HaxeArrayAny).getDynamic(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native private static Object getInstance(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static Object get(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
	    Type elementType = getArrayElementType(array.getClass());
	    if (elementType == Boolean.TYPE) {
		    return getBoolean(array, index);
	    } else if (elementType == Byte.TYPE) {
		    return getByte(array, index);
	    } else if (elementType == Character.TYPE) {
		    return getChar(array, index);
	    } else if (elementType == Short.TYPE) {
		    return getShort(array, index);
	    } else if (elementType == Integer.TYPE) {
		    return getInt(array, index);
	    } else if (elementType == Long.TYPE) {
		    return getLong(array, index);
	    } else if (elementType == Float.TYPE) {
		    return getFloat(array, index);
	    } else if (elementType == Double.TYPE) {
		    return getDouble(array, index);
	    } else {
		    return getInstance(array, index);
	    }
    }

    @HaxeMethodBody("return cast(p0, HaxeArrayBool).getBool(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1) != 0;")
	native public static boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("return cast(p0, HaxeArrayByte).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native public static byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("return cast(p0, HaxeArrayChar).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native public static char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("return cast(p0, HaxeArrayShort).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native public static short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("return cast(p0, HaxeArrayInt).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native public static int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("return cast(p0, HaxeArrayLong).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native public static long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("return cast(p0, HaxeArrayFloat).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native public static float getFloat(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("return cast(p0, HaxeArrayDouble).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native public static double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;


	@HaxeMethodBody("cast(p0, HaxeArrayAny).setDynamic(p1, p2);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	native private static void setInstance(Object array, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	public static void set(Object array, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		Type elementType = getArrayElementType(array.getClass());
		if (elementType == Boolean.TYPE) {
			setBoolean(array, index, (Boolean) value);
		} else if (elementType == Byte.TYPE) {
			setByte(array, index, (Byte) value);
		} else if (elementType == Character.TYPE) {
			setChar(array, index, (Character) value);
		} else if (elementType == Short.TYPE) {
			setShort(array, index, (Short) value);
		} else if (elementType == Integer.TYPE) {
			setInt(array, index, (Integer) value);
		} else if (elementType == Long.TYPE) {
			setLong(array, index, (Long) value);
		} else if (elementType == Float.TYPE) {
			setFloat(array, index, (Float) value);
		} else if (elementType == Double.TYPE) {
			setDouble(array, index, (Double) value);
		} else {
			setInstance(array, index, value);
		}
	}

    @HaxeMethodBody("cast(p0, HaxeArrayBool).setBool(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	native public static void setBoolean(Object array, int index, boolean z) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("cast(p0, HaxeArrayByte).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	native public static void setByte(Object array, int index, byte b) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("cast(p0, HaxeArrayChar).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	native public static void setChar(Object array, int index, char c) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("cast(p0, HaxeArrayShort).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	native public static void setShort(Object array, int index, short s) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("cast(p0, HaxeArrayInt).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	native public static void setInt(Object array, int index, int i) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("cast(p0, HaxeArrayLong).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	native public static void setLong(Object array, int index, long l) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("cast(p0, HaxeArrayFloat).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	native public static void setFloat(Object array, int index, float f) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	@HaxeMethodBody("cast(p0, HaxeArrayDouble).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	native public static void setDouble(Object array, int index, double d) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

	static private Type getArrayElementType(Class<?> clazz) {
		String name = clazz.getName();
		if (name.charAt(0) != '[') throw new RuntimeException("Not an array");
		try {
			return Class.forName(name.substring(1));
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
