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

public class AtomicInteger extends Number implements java.io.Serializable {
	private volatile int value;

	public AtomicInteger(int initialValue) {
		value = initialValue;
	}

	public AtomicInteger() {
	}

	public final int get() {
		return value;
	}

	public final void set(int newValue) {
		value = newValue;
	}

	public final void lazySet(int newValue) {
		value = newValue;
	}

	public final int getAndSet(int newValue) {
		int old = this.value;
		this.value = newValue;
		return old;
	}

	private int _setAndSet(int newValue) {
		return this.value = newValue;
	}

	public final boolean compareAndSet(int expect, int update) {
		if (this.value == expect) {
			this.value = update;
			return true;
		} else {
			return false;
		}
	}

	public final boolean weakCompareAndSet(int expect, int update) {
		return compareAndSet(expect, update);
	}

	public final int getAndIncrement() {
		return getAndSet(this.value + 1);
	}

	public final int getAndDecrement() {
		return getAndSet(this.value - 1);
	}

	public final int getAndAdd(int delta) {
		return getAndSet(this.value + delta);
	}

	public final int incrementAndGet() {
		return _setAndSet(this.value + 1);
	}

	public final int decrementAndGet() {
		return _setAndSet(this.value - 1);
	}

	public final int addAndGet(int delta) {
		return _setAndSet(this.value + delta);
	}

	public String toString() {
		return Integer.toString(get());
	}

	public long longValue() {
		return (long) get();
	}

	public int intValue() {
		return get();
	}

	public float floatValue() {
		return (float) get();
	}

	public double doubleValue() {
		return (double) get();
	}
}
