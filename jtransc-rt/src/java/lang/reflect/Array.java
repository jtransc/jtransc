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
	private static boolean[] newBoolInstance(int length) throws NegativeArraySizeException {
		return new boolean[length];
	}

	@HaxeMethodBody("return new HaxeArrayByte(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_B(p0);")
	private static byte[] newByteInstance(int length) throws NegativeArraySizeException {
		return new byte[length];
	}

	@HaxeMethodBody("return new HaxeArrayChar(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_C(p0);")
	private static char[] newCharInstance(int length) throws NegativeArraySizeException {
		return new char[length];
	}

	@HaxeMethodBody("return new HaxeArrayShort(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_S(p0);")
	private static short[] newShortInstance(int length) throws NegativeArraySizeException {
		return new short[length];
	}

	@HaxeMethodBody("return new HaxeArrayInt(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_I(p0);")
	private static int[] newIntInstance(int length) throws NegativeArraySizeException {
		return new int[length];
	}

	@HaxeMethodBody("return new HaxeArrayLong(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_J(p0);")
	private static long[] newLongInstance(int length) throws NegativeArraySizeException {
		return new long[length];
	}

	@HaxeMethodBody("return new HaxeArrayFloat(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_F(p0);")
	private static float[] newFloatInstance(int length) throws NegativeArraySizeException {
		return new float[length];
	}

	@HaxeMethodBody("return new HaxeArrayDouble(p0);")
	@JTranscMethodBody(target = "js", value = "return new JA_D(p0);")
	private static Object newDoubleInstance(int length) throws NegativeArraySizeException {
		return new double[length];
	}

	@HaxeMethodBody("return new HaxeArrayAny(p0, p1._str);")
	@JTranscMethodBody(target = "js", value = "return new JA_L(p0, N.istr(p1));")
	@JTranscMethodBody(target = "cpp", value = "return SOBJ(new JA_L(p0, N::istr2(p1)));")
	native private static Object newObjectInstance(int length, String desc) throws NegativeArraySizeException;

	public static Object newInstance(Class<?> type, int length) throws NegativeArraySizeException {
		if (type == null) throw new NullPointerException("Array.newInstance");

		if (!type.isPrimitive()) return newObjectInstance(length, "[" + type.getName());
		if (type == Boolean.TYPE) return newBoolInstance(length);
		if (type == Byte.TYPE) return newByteInstance(length);
		if (type == Short.TYPE) return newShortInstance(length);
		if (type == Character.TYPE) return newCharInstance(length);
		if (type == Integer.TYPE) return newIntInstance(length);
		if (type == Long.TYPE) return newLongInstance(length);
		if (type == Float.TYPE) return newFloatInstance(length);
		if (type == Double.TYPE) return newDoubleInstance(length);
		if (type == Void.TYPE) throw new RuntimeException("Invalid Array of void type");
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
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_0, p0)->length;")
	native public static int getLength(Object array) throws IllegalArgumentException;


	@HaxeMethodBody("return cast(p0, HaxeArrayAny).getDynamic(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_L, p0)->get(p1);")
	native private static Object getInstance(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    public static Object get(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
	    Type elementType = getArrayElementType(array.getClass());
	    if (elementType == Boolean.TYPE) return getBoolean(array, index);
	    if (elementType == Byte.TYPE) return getByte(array, index);
	    if (elementType == Character.TYPE) return getChar(array, index);
	    if (elementType == Short.TYPE) return getShort(array, index);
	    if (elementType == Integer.TYPE) return getInt(array, index);
	    if (elementType == Long.TYPE) return getLong(array, index);
	    if (elementType == Float.TYPE) return getFloat(array, index);
	    if (elementType == Double.TYPE) return getDouble(array, index);
	    return getInstance(array, index);
    }

    @HaxeMethodBody("return cast(p0, HaxeArrayBool).getBool(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1) != 0;")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_Z, p0)->get(p1);")
	public static boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		return ((boolean[])array)[index];
	}

	@HaxeMethodBody("return cast(p0, HaxeArrayByte).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_B, p0)->get(p1);")
	public static byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		return ((byte[])array)[index];
	}

	@HaxeMethodBody("return cast(p0, HaxeArrayChar).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_C, p0)->get(p1);")
	public static char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		return ((char[])array)[index];
	}

	@HaxeMethodBody("return cast(p0, HaxeArrayShort).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_S, p0)->get(p1);")
	public static short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		return ((short[])array)[index];
	}

	@HaxeMethodBody("return cast(p0, HaxeArrayInt).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_I, p0)->get(p1);")
	public static int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		return ((int[])array)[index];
	}

	@HaxeMethodBody("return cast(p0, HaxeArrayLong).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_J, p0)->get(p1);")
	public static long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		return ((long[])array)[index];
	}

	@HaxeMethodBody("return cast(p0, HaxeArrayFloat).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_F, p0)->get(p1);")
	public static float getFloat(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		return ((float[])array)[index];
	}

	@HaxeMethodBody("return cast(p0, HaxeArrayDouble).get(p1);")
	@JTranscMethodBody(target = "js", value = "return p0.get(p1);")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE(JA_D, p0)->get(p1);")
	public static double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		return ((double[])array)[index];
	}


	@HaxeMethodBody("cast(p0, HaxeArrayAny).setDynamic(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_L, p0)->set(p1, p2);")
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
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_Z, p0)->set(p1, p2);")
	public static void setBoolean(Object array, int index, boolean v) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		((boolean[])array)[index] = v;
	}

	@HaxeMethodBody("cast(p0, HaxeArrayByte).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_B, p0)->set(p1, p2);")
	public static void setByte(Object array, int index, byte v) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		((byte[])array)[index] = v;
	}

	@HaxeMethodBody("cast(p0, HaxeArrayChar).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_C, p0)->set(p1, p2);")
	public static void setChar(Object array, int index, char v) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		((char[])array)[index] = v;
	}

	@HaxeMethodBody("cast(p0, HaxeArrayShort).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_S, p0)->set(p1, p2);")
	public static void setShort(Object array, int index, short v) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		((short[])array)[index] = v;
	}

	@HaxeMethodBody("cast(p0, HaxeArrayInt).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_I, p0)->set(p1, p2);")
	public static void setInt(Object array, int index, int v) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		((int[])array)[index] = v;
	}

	@HaxeMethodBody("cast(p0, HaxeArrayLong).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_J, p0)->set(p1, p2);")
	public static void setLong(Object array, int index, long v) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		((long[])array)[index] = v;
	}

	@HaxeMethodBody("cast(p0, HaxeArrayFloat).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_F, p0)->set(p1, p2);")
	public static void setFloat(Object array, int index, float v) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		((float[])array)[index] = v;
	}

	@HaxeMethodBody("cast(p0, HaxeArrayDouble).set(p1, p2);")
	@JTranscMethodBody(target = "js", value = "p0.set(p1, p2);")
	@JTranscMethodBody(target = "cpp", value = "GET_OBJECT_NPE(JA_D, p0)->set(p1, p2);")
	public static void setDouble(Object array, int index, double v) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
		((double[])array)[index] = v;
	}

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
