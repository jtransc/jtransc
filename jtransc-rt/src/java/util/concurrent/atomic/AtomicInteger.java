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

import com.jtransc.annotation.JTranscMethodBody;

@SuppressWarnings({"SameParameterValue", "WeakerAccess"})
public class AtomicInteger extends Number implements java.io.Serializable {
	private volatile int value;

	public AtomicInteger(int initialValue) {
		value = initialValue;
	}

	public AtomicInteger() {
	}

	// http://dlang.org/library/core/atomic.html
	//MemoryOrder:
	//acq	Hoist-load + hoist-store barrier.
	//raw	Not sequenced.
	//rel	Sink-load + sink-store barrier.
	//seq	Fully sequenced (acquire + release).

	public final int get() {
		return value;
	}

	public final void set(int newValue) {
		value = newValue;
	}

	public final void lazySet(int newValue) {
		set(newValue);
	}

	public final boolean compareAndSet(int expect, int update) {
		if (get() == expect) {
			set(update);
			return true;
		} else {
			return false;
		}
	}

	private int _addAndGet(int delta) {
		return this.value = get() + delta;
	}

	public final int addAndGet(int delta) {
		return _addAndGet(delta);
	}

	public final int getAndAdd(int delta) {
		return _addAndGet(delta) - delta;
	}

	public final int getAndSet(int newValue) {
		int old = get();
		set(newValue);
		return old;
	}

	public final boolean weakCompareAndSet(int expect, int update) {
		return compareAndSet(expect, update);
	}

	public final int getAndIncrement() {
		return getAndAdd(+1);
	}

	public final int getAndDecrement() {
		return getAndAdd(-1);
	}

	public final int incrementAndGet() {
		return addAndGet(+1);
	}

	public final int decrementAndGet() {
		return addAndGet(-1);
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
