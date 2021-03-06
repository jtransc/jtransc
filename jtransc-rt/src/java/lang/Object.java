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

import com.jtransc.annotation.*;




import java.lang.jtransc.JTranscCoreReflection;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

@SuppressWarnings({"WeakerAccess", "unused"})
@JTranscAddFile(target = "js", priority = -1, process = true, prependAppend = "js/Base.js")
@JTranscAddFile(target = "dart", priority = -1, process = true, prependAppend = "dart/Base.dart")
@JTranscAddFile(target = "dart", priority = -1, process = true, src = "dart/pubspec.yaml", dst = "pubspec.yaml")
@JTranscAddFile(target = "cpp", priority = -1, process = true, src = "cpp/GC.cpp", dst = "GC.cpp")
@JTranscAddFile(target = "cpp", priority = -1, process = true, src = "cpp/JNI.cpp", dst = "JNI.cpp")
@JTranscAddFile(target = "cpp", priority = -1, process = true, src = "cpp/CMakeLists.txt", dst = "CMakeLists.txt")
@JTranscAddFile(target = "cpp", priority = -1, process = true, src = "cpp/combined_jni.h", dst = "jni.h")
@JTranscAddFile(target = "cs", priority = -1, process = true, prependAppend = "cs/Base.cs")
@JTranscAddFile(target = "cs", priority = -1, process = true, src = "cs/program.csproj", dst = "program.csproj")
@JTranscAddMembers(target = "cs", value = "public int __id = 0;")
public class Object {
	public Object() {
	}

	public boolean __equalsSync__(Object obj) {
		return (this == obj);
	}

	public boolean equals(Object obj) {
		return __equalsSync__(obj);
	}

	// @TODO: All object could have class descriptor eg. [I
	public final Class<?> getClass() {
		if (JTranscCoreReflection.isArray(this)) {
			return JTranscCoreReflection.getClassByName(JTranscCoreReflection.getArrayDescriptor(this));
		} else {
			return JTranscCoreReflection.getClassById(JTranscCoreReflection.getClassId(this));
		}
	}

	public int hashCode() {
		return SystemInt.identityHashCode(this);
	}

	@SuppressWarnings("SuspiciousSystemArraycopy")
	//@JTranscKeep
	@JTranscMethodBody(target = "js", value = "return N.clone(this);")
	protected Object clone() throws CloneNotSupportedException {
		if (JTranscCoreReflection.isArray(this)) {
			int len = Array.getLength(this);
			Object o = Array.newInstance(this.getClass().getComponentType(), len);
			//noinspection SuspiciousSystemArraycopy
			System.arraycopy(this, 0, o, 0, len);
			return o;
		} else {
			// @TODO: This is slow! We could override this at code gen knowing all the fields and with generated code to generate them.
			try {
				Class<?> clazz = this.getClass();
				Object newObject = clazz.newInstance();
				for (Field field : clazz.getDeclaredFields()) {
					field.set(newObject, field.get(this));
				}
				return newObject;
			} catch (Throwable e) {
				throw new CloneNotSupportedException(e.toString());
			}
		}
	}

	@JTranscKeep
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(this.hashCode());
	}

	public final void wait(long timeout) throws InterruptedException {
	}

	public final void wait() throws InterruptedException {
	}

	public final void wait(long timeout, int nanos) throws InterruptedException {
	}

	public final void notify() {
	}

	public final void notifyAll() {
	}

	protected void finalize() throws Throwable {
	}
}
