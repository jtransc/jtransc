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

public class AtomicLong extends Number implements java.io.Serializable {
	private volatile long value;

	public AtomicLong(long initialValue) {
		value = initialValue;
	}

	public AtomicLong() {
	}

	public final long get() {
		return value;
	}

	public final void set(long newValue) {
		value = newValue;
	}

	public final void lazySet(long newValue) {
		value = newValue;
	}

	public final long getAndSet(long newValue) {
		long old = this.value;
		this.value = newValue;
		return old;
	}

	private long _setAndSet(long newValue) {
		return this.value = newValue;
	}

	public final boolean compareAndSet(long expect, long update) {
		if (this.value == expect) {
			this.value = update;
			return true;
		} else {
			return false;
		}
	}

	public final boolean weakCompareAndSet(long expect, long update) {
		return compareAndSet(expect, update);
	}

	public final long getAndIncrement() {
		return getAndSet(this.value + 1);
	}

	public final long getAndDecrement() {
		return getAndSet(this.value - 1);
	}

	public final long getAndAdd(long delta) {
		return getAndSet(this.value + delta);
	}

	public final long incrementAndGet() {
		return _setAndSet(this.value + 1);
	}

	public final long decrementAndGet() {
		return _setAndSet(this.value - 1);
	}

	public final long addAndGet(long delta) {
		return _setAndSet(this.value + delta);
	}

	public String toString() {
		return Long.toString(get());
	}

	public int intValue() {
		return (int) get();
	}

	public long longValue() {
		return get();
	}

	public float floatValue() {
		return (float) get();
	}

	public double doubleValue() {
		return (double) get();
	}
}
