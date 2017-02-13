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
import com.jtransc.annotation.haxe.HaxeAddSubtarget;

import java.lang.jtransc.JTranscCoreReflection;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

@SuppressWarnings({"WeakerAccess", "unused"})
@HaxeAddFilesTemplate(base = "hx", value = {
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
@JTranscAddFile(target = "js", priority = -1, process = true, prependAppend = "js/Runtime.js")
@JTranscAddFile(target = "d", priority = -1, process = true, prependAppend = "d/Base.d")
@JTranscAddFile(target = "cs", priority = -1, process = true, prependAppend = "cs/Base.cs")
@JTranscAddFile(target = "php", priority = -1, process = true, prependAppend = "php/Base.php")
@JTranscAddMembers(target = "d", value = {
	"core.sync.mutex.Mutex __d_mutex = null;",
})
public class Object {
	@JTranscInvisible
	static private int $$lastId = 0;

	@JTranscInvisible
	public int $$id;

	public Object() {
		$$id = $$lastId++;
	}

	//@JTranscKeep
	public boolean equals(Object obj) {
		return (this == obj);
	}

	// @TODO: All object could have class descriptor eg. [I
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

	public final void notify() {
	}

	public final void notifyAll() {
	}

	public final void wait(long timeout) throws InterruptedException {
	}

	public final void wait(long timeout, int nanos) throws InterruptedException {
		wait(timeout);
	}

	public final void wait() throws InterruptedException {
		wait(0);
	}

	protected void finalize() throws Throwable {
	}
}
