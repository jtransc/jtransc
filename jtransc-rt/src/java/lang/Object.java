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

import jtransc.annotation.JTranscKeep;
import jtransc.annotation.haxe.HaxeAddFiles;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

import java.io.IOException;

@HaxeAddMembers({
        "static public var __LAST_ID__ = 0;",
        "public var __ID__ = __LAST_ID__++;",
})
@HaxeAddFiles({
        "HaxeNatives.hx",
        "HaxeFormat.hx",
        "HaxeNativeWrapper.hx",
        "HaxeBaseArray.hx",
		"HaxeBoolArray.hx",
        "HaxeByteArray.hx",
        "HaxeShortArray.hx",
        "HaxeIntArray.hx",
        "HaxeFloatArray.hx",
        "HaxeDoubleArray.hx",
        "HaxeLongArray.hx",
        "HaxeArray.hx"
})
public class Object {
	@JTranscKeep
	public boolean equals(Object obj) {
		return (this == obj);
	}

    @HaxeMethodBody("return HaxeNatives.getClass(this);")
	native public final Class<?> getClass();

	@JTranscKeep
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@JTranscKeep
	native protected Object clone() throws CloneNotSupportedException;

	@JTranscKeep
	public String toString() {
		Class<?> clazz = getClass();
		String hashCode = Integer.toHexString(this.hashCode());
		if (clazz != null) {
			return clazz.getName() + "@" + hashCode;
		} else {
			return "null@" + hashCode;
		}
	}

	public final void notify() {

	}

	public final void notifyAll() {

	}

	public final void wait(long timeout) {

	}

	public final void wait(long timeout, int nanos) {

	}

	public final void wait() {

	}

	protected void finalize() throws IOException {

	}
}
