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

package java.lang;

public final class Boolean implements java.io.Serializable, Comparable<Boolean> {
    public static final Boolean TRUE = new Boolean(true);
    public static final Boolean FALSE = new Boolean(false);

    public static final Class<Boolean> TYPE = (Class<Boolean>) Class.getPrimitiveClass("boolean");

    private boolean value;

    public Boolean(boolean value) {
        this.value = value;
    }

    public Boolean(String value) {
        this.value = parseBoolean(value);
    }

    public static boolean parseBoolean(String value) {
        return (value != null) ? (value.compareToIgnoreCase("true") == 0) : false;
    }

    public boolean booleanValue() {
        return value;
    }

    public static Boolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static Boolean valueOf(String value) {
        return valueOf(parseBoolean(value));
    }

    public static String toString(boolean value) {
        return value ? "true" : "false";
    }

    public String toString() {
        return toString(value);
    }

    @Override
    public int hashCode() {
        return hashCode(value);
    }

    public static int hashCode(boolean value) {
        return value ? 1 : 0;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (that == null) return false;
        if (getClass() != that.getClass()) return false;
        return value == ((Boolean) that).value;
    }

    public static boolean getBoolean(String name) {
        return parseBoolean(System.getProperty(name));
    }

    public int compareTo(Boolean that) {
        return (that != null) ? compare(this.value, that.value) : compare(this.value, false);
    }

    public static int compare(boolean l, boolean r) {
        return (l == r) ? 0 : ((!l) ? -1 : +1);
    }

    public static boolean logicalAnd(boolean l, boolean r) {
        return l & r;
    }

    public static boolean logicalOr(boolean l, boolean r) {
        return l | r;
    }

    public static boolean logicalXor(boolean l, boolean r) {
        return l ^ r;
    }
}
