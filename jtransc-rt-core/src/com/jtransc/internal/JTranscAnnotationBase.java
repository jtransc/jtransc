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

package com.jtransc.internal;

import com.jtransc.annotation.JTranscInvisible;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@JTranscInvisible
abstract public class JTranscAnnotationBase {
	@Override
	public String toString() {
		return toStaticString(this);

	}

	@SuppressWarnings("all")
	static public String toStaticString(Object annotation) {
		String out = "";
		out += "@";
		out += annotation.getClass().getName();
		out += "(";
		int n = 0;
		for (Method method : annotation.getClass().getDeclaredMethods()) {
			if (n != 0) out += ", ";
			try {
				out += method.getName() + "=" + method.invoke(annotation);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			n++;
		}
		out += ")";
		return out;
	}
}
