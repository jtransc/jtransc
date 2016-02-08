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

public final class Array {
    private Array() {
    }

    native public static Object newInstance(Class<?> componentType, int length) throws NegativeArraySizeException;

    native public static Object newInstance(Class<?> componentType, int... dimensions) throws IllegalArgumentException, NegativeArraySizeException;

    native public static int getLength(Object array) throws IllegalArgumentException;

    native public static Object get(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static boolean getBoolean(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static byte getByte(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static char getChar(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static short getShort(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static int getInt(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static long getLong(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static float getFloat(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static double getDouble(Object array, int index) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void set(Object array, int index, Object value) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void setBoolean(Object array, int index, boolean z) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void setByte(Object array, int index, byte b) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void setChar(Object array, int index, char c) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void setShort(Object array, int index, short s) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void setInt(Object array, int index, int i) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void setLong(Object array, int index, long l) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void setFloat(Object array, int index, float f) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;

    native public static void setDouble(Object array, int index, double d) throws IllegalArgumentException, ArrayIndexOutOfBoundsException;
}
