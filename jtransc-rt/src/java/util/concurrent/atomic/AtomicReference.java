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

package java.util.concurrent.atomic;

@SuppressWarnings({"unused", "WeakerAccess"})
public class AtomicReference<V> implements java.io.Serializable {
	private V value = null;

	public AtomicReference(V initialValue) {
		this.value = initialValue;
	}

	public AtomicReference() {
	}

	public final V get() {
		return value;
	}

	public final void set(V newValue) {
		this.value = newValue;
	}

	public final void lazySet(V newValue) {
		this.set(newValue);
	}

	public final boolean compareAndSet(V expect, V update) {
		if (this.value != expect) return false;
		this.value = update;
		return true;
	}

	public final boolean weakCompareAndSet(V expect, V update) {
		return this.compareAndSet(expect, update);
	}

	public final V getAndSet(V newValue) {
		V old = this.value;
		this.value = newValue;
		return old;
	}

	public String toString() {
		return "AtomicReference";
	}
}
