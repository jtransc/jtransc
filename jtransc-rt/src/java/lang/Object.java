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

import java.io.IOException;

public class Object {
	@JTranscKeep
	public boolean equals(Object obj) {
		return (this == obj);
	}

	native public final Class<?> getClass();

	@JTranscKeep
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@JTranscKeep
	native protected Object clone() throws CloneNotSupportedException;

	@JTranscKeep
	public String toString() {
		return getClass().getName() + "@" + Integer.toHexString(hashCode());
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
