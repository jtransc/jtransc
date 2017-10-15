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


import com.jtransc.annotation.JTranscSync;

public class Modifier {
	@JTranscSync
	public static boolean isPublic(int mod) {
		return (mod & PUBLIC) != 0;
	}

	@JTranscSync
	public static boolean isPrivate(int mod) {
		return (mod & PRIVATE) != 0;
	}

	@JTranscSync
	public static boolean isProtected(int mod) {
		return (mod & PROTECTED) != 0;
	}

	@JTranscSync
	public static boolean isStatic(int mod) {
		return (mod & STATIC) != 0;
	}

	@JTranscSync
	public static boolean isFinal(int mod) {
		return (mod & FINAL) != 0;
	}

	@JTranscSync
	public static boolean isSynchronized(int mod) {
		return (mod & SYNCHRONIZED) != 0;
	}

	@JTranscSync
	public static boolean isVolatile(int mod) {
		return (mod & VOLATILE) != 0;
	}

	@JTranscSync
	public static boolean isTransient(int mod) {
		return (mod & TRANSIENT) != 0;
	}

	@JTranscSync
	public static boolean isNative(int mod) {
		return (mod & NATIVE) != 0;
	}

	@JTranscSync
	public static boolean isInterface(int mod) {
		return (mod & INTERFACE) != 0;
	}

	@JTranscSync
	public static boolean isAbstract(int mod) {
		return (mod & ABSTRACT) != 0;
	}

	@JTranscSync
	public static boolean isStrict(int mod) {
		return (mod & STRICT) != 0;
	}

	@JTranscSync
	public static boolean isSynthetic(int mod) {
		return (mod & SYNTHETIC) != 0;
	}

	@JTranscSync
	public static String toString(int mod) {
		StringBuilder sb = new StringBuilder();
		int len;

		if ((mod & PUBLIC) != 0) sb.append("public ");
		if ((mod & PROTECTED) != 0) sb.append("protected ");
		if ((mod & PRIVATE) != 0) sb.append("private ");

        /* Canonical order */
		if ((mod & ABSTRACT) != 0) sb.append("abstract ");
		if ((mod & STATIC) != 0) sb.append("static ");
		if ((mod & FINAL) != 0) sb.append("final ");
		if ((mod & TRANSIENT) != 0) sb.append("transient ");
		if ((mod & VOLATILE) != 0) sb.append("volatile ");
		if ((mod & SYNCHRONIZED) != 0) sb.append("synchronized ");
		if ((mod & NATIVE) != 0) sb.append("native ");
		if ((mod & STRICT) != 0) sb.append("strictfp ");
		if ((mod & INTERFACE) != 0) sb.append("interface ");

		if ((len = sb.length()) > 0)    /* trim trailing space */
			return sb.toString().substring(0, len - 1);
		return "";
	}

	public static final int PUBLIC = 0x00000001;
	public static final int PRIVATE = 0x00000002;
	public static final int PROTECTED = 0x00000004;
	public static final int STATIC = 0x00000008;
	public static final int FINAL = 0x00000010;
	public static final int SYNCHRONIZED = 0x00000020;
	public static final int VOLATILE = 0x00000040;
	public static final int TRANSIENT = 0x00000080;
	public static final int NATIVE = 0x00000100;
	public static final int INTERFACE = 0x00000200;
	public static final int ABSTRACT = 0x00000400;
	public static final int STRICT = 0x00000800;
	public static final int SYNTHETIC = 0x00001000;

	public static final int BRIDGE = 0x00000040;
	public static final int VARARGS = 0x00000080;
	public static final int ANNOTATION = 0x00002000;
	public static final int ENUM = 0x00004000;
	public static final int MANDATED = 0x00008000;
}
