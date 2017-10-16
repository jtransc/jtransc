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
import com.jtransc.annotation.haxe.HaxeAddFilesTemplate;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeAddSubtarget;

import java.lang.jtransc.JTranscCoreReflection;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

@SuppressWarnings({"WeakerAccess", "unused"})
@HaxeAddFilesTemplate(base = "hx", value = {
	"hx/MyStringBuf.hx",
	"hx/N.hx", "hx/Float32.hx", "hx/Float64.hx",
	"hx/JA_0.hx", "hx/JA_B.hx", "hx/JA_C.hx", "hx/JA_D.hx", "hx/JA_F.hx", "hx/JA_I.hx", "hx/JA_J.hx", "hx/JA_L.hx", "hx/JA_S.hx", "hx/JA_Z.hx",
	"hx/HaxePolyfills.hx", "hx/HaxeDynamicLoad.hx", "hx/HaxeIO.hx", "hx/HaxeNativeWrapper.hx",
})
@HaxeAddSubtarget(name = "js", alias = {"default", "javascript"}, cmdSwitch = "-js", singleFile = true, interpreter = "node", extension = "js")
@HaxeAddSubtarget(name = "cpp", alias = {"c", "c++"}, cmdSwitch = "-cpp", singleFile = true, interpreter = "", extension = "exe")
@HaxeAddSubtarget(name = "swf", alias = {"flash", "as3"}, cmdSwitch = "-swf", singleFile = true, interpreter = "", extension = "swf")
@HaxeAddSubtarget(name = "neko", cmdSwitch = "-neko", singleFile = true, interpreter = "neko", extension = "n")
@HaxeAddSubtarget(name = "php", cmdSwitch = "-php", singleFile = false, interpreter = "php", extension = "php", interpreterSuffix = "/index.php")
@HaxeAddSubtarget(name = "cs", cmdSwitch = "-cs", singleFile = true, interpreter = "", extension = "exe")
@HaxeAddSubtarget(name = "java", cmdSwitch = "-java", singleFile = true, interpreter = "java -jar", extension = "jar")
@HaxeAddSubtarget(name = "python", cmdSwitch = "-python", singleFile = true, interpreter = "python", extension = "py")
@JTranscAddFile(target = "js", priority = -1, process = true, prependAppend = "js/Base.js")
@JTranscAddFile(target = "d", priority = -1, process = true, prependAppend = "d/Base.d")
@JTranscAddFile(target = "cs", priority = -1, process = true, prependAppend = "cs/Base.cs")
@JTranscAddFile(target = "dart", priority = -1, process = true, prependAppend = "dart/Base.dart")
@JTranscAddFile(target = "dart", priority = -1, process = true, src = "dart/pubspec.yaml", dst = "pubspec.yaml")
@JTranscAddFile(target = "php", priority = -1, process = true, prependAppend = "php/Base.php")
@JTranscAddFile(target = "cpp", priority = -1, process = true, src = "cpp/GC_portable.cpp", dst = "GC_portable.cpp")
@JTranscAddFile(target = "cpp", priority = -1, process = true, src = "cpp/GC_boehm.cpp", dst = "GC_boehm.cpp")
@JTranscAddFile(target = "cpp", priority = -1, process = true, src = "cpp/CMakeLists.txt", dst = "CMakeLists.txt")
@JTranscAddFile(target = "cpp", priority = -1, process = true, src = "cpp/combined_jni.h", dst = "jni.h")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/_project.as3proj", dst = "_project.as3proj")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/N.as", dst = "N.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/Main.as", dst = "Main.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_0.as", dst = "JA_0.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_Z.as", dst = "JA_Z.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_B.as", dst = "JA_B.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_C.as", dst = "JA_C.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_S.as", dst = "JA_S.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_I.as", dst = "JA_I.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_J.as", dst = "JA_J.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_F.as", dst = "JA_F.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_D.as", dst = "JA_D.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/JA_L.as", dst = "JA_L.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/Int32.as", dst = "Int32.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/Int64.as", dst = "Int64.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/DivModResult.as", dst = "DivModResult.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/WrappedThrowable.as", dst = "WrappedThrowable.as")
@JTranscAddFile(target = "as3", priority = -1, process = true, src = "as3/Main.xml", dst = "Main.xml")
@JTranscAddMembers(target = "d", value = "core.sync.mutex.Mutex __d_mutex = null;")
@JTranscAddMembers(target = "cpp", value = "std::recursive_mutex mtx;")
@HaxeAddMembers({
	"#if cpp public var _hx_mutex: cpp.vm.Mutex = null; #end",
})
public class Object {
	@JTranscInvisible
	public int $$id;

	@JTranscSync
	public Object() {
	}

	@JTranscSync
	public boolean __equalsSync__(Object obj) {
		return (this == obj);
	}

	public boolean equals(Object obj) {
		return __equalsSync__(obj);
	}

	// @TODO: All object could have class descriptor eg. [I
	@JTranscSync
	public final Class<?> getClass() {
		if (JTranscCoreReflection.isArray(this)) {
			return JTranscCoreReflection.getClassByName(JTranscCoreReflection.getArrayDescriptor(this));
		} else {
			return JTranscCoreReflection.getClassById(JTranscCoreReflection.getClassId(this));
		}
	}

	//@JTranscKeep
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
	//@JTranscMethodBody(target = "js", value = "return N.str('Object');")
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(this.hashCode());
	}

	private static final long SAMPLING_STEP = 50;
	private long waitTimeout;

	@JTranscAsync
	public final void wait(long timeout) throws InterruptedException {
		wait(timeout, 0);
	}

	@JTranscAsync
	public final void wait() throws InterruptedException {
		wait(0L, 0);
	}

	@JTranscMethodBody(target = "js", value = "{{ AWAIT }} N.threadWait({{ JC_COMMA }}this, N.j2d(p0), p1);", async = true)
	@JTranscAsync
	public final void wait(long timeout, int nanos) throws InterruptedException {
		if (timeout < 0)
			throw new IllegalArgumentException("timeout is negative");
		waitTimeout = timeout == 0 ? Long.MAX_VALUE : timeout;
		while (waitTimeout > 0) {
			waitTimeout -= SAMPLING_STEP;
			Thread.sleep(SAMPLING_STEP);
		}
	}

	@JTranscMethodBody(target = "js", value = "{{ AWAIT }} N.threadNotify({{ JC_COMMA }}this);", async = true)
	@JTranscAsync
	public final void notify() {
		waitTimeout = 0;
	}

	@JTranscMethodBody(target = "js", value = "{{ AWAIT }} N.threadNotifyAll({{ JC_COMMA }}this);", async = true)
	@JTranscAsync
	public final void notifyAll() {
		waitTimeout = 0;
	}


	protected void finalize() throws Throwable {
	}
}
