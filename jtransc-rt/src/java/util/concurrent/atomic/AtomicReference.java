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

public class AtomicReference<V> implements java.io.Serializable {
	public AtomicReference(V initialValue) {

	}

	public AtomicReference() {
	}

	native public final V get();

	native public final void set(V newValue);

	native public final void lazySet(V newValue);

	native public final boolean compareAndSet(V expect, V update);

	native public final boolean weakCompareAndSet(V expect, V update);

	native public final V getAndSet(V newValue);

	native public String toString();
}
