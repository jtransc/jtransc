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

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.annotation.JTranscVisible;
import j.MemberInfo;

import java.lang.annotation.Annotation;

abstract public class AccessibleObject implements AnnotatedElement {
	@JTranscVisible
	public MemberInfo info;

	@JTranscSync
	AccessibleObject(MemberInfo info) {
		this.info = info;
	}

	@JTranscSync
	public static void setAccessible(AccessibleObject[] array, boolean flag) {
		for (AccessibleObject o : array) o.setAccessible(flag);
	}

	@JTranscSync
	public void setAccessible(boolean flag) throws SecurityException {
	}

	@JTranscSync
	public boolean isAccessible() {
		return (info.modifiers & Modifier.PUBLIC) != 0;
	}

	@JTranscSync
	protected AccessibleObject() {
	}

	@JTranscSync
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getAnnotation(annotationClass) != null;
	}

	@JTranscSync
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		for (Annotation annotation : getDeclaredAnnotations()) {
			if (annotation.getClass() == annotationClass) return (T) annotation;
		}
		return null;
	}

	@JTranscSync
	public Annotation[] getAnnotations() {
		return this.getDeclaredAnnotations(); // @TODO: Fix me!
	}

	@JTranscSync
	abstract public Annotation[] getDeclaredAnnotations();
}
