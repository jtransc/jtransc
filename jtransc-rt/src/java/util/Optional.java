/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Optional<T> {
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static Optional<?> CACHED_EMPTY;

	private final T v;

	private Optional(T value) {
		this.v = value;
	}

	public static <T> Optional<T> of(T value) {
		return new Optional<>(value);
	}

	public static <T> Optional<T> ofNullable(T value) {
		return (value != null) ? of(value) : empty();
	}

	public static <T> Optional<T> empty() {
		if (CACHED_EMPTY == null) CACHED_EMPTY = new Optional<>(null);
		//noinspection unchecked
		return (Optional<T>) CACHED_EMPTY;
	}

	public boolean isPresent() {
		return v != null;
	}

	public T get() {
		if (v == null) throw new NoSuchElementException("No value present");
		return v;
	}

	public void ifPresent(Consumer<? super T> consumer) {
		if (v != null) consumer.accept(v);
	}

	public T orElse(T defaultValue) {
		return v != null ? v : defaultValue;
	}

	public T orElseGet(Supplier<? extends T> defaultSupplier) {
		return v != null ? v : defaultSupplier.get();
	}

	public <X extends Throwable> T orElseThrow(Supplier<? extends X> throwSupplier) throws X {
		//noinspection unchecked
		return (v != null) ? v : (T) throwSupplier.get();
	}

	public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
		return isPresent() ? Optional.ofNullable(mapper.apply(v)) : empty();
	}

	public <U> Optional<U> flatMap(Function<? super T, Optional<U>> isPresentMapper) {
		return isPresent() ? Objects.requireNonNull(isPresentMapper.apply(v)) : empty();
	}

	public Optional<T> filter(Predicate<? super T> predicate) {
		return isPresent() ? (predicate.test(v) ? this : empty()) : this;
	}

	@Override
	public boolean equals(Object that) {
		return this == that || that instanceof Optional && Objects.equals(v, ((Optional<?>) that).v);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(v);
	}

	@Override
	public String toString() {
		return (v != null) ? ("Optional[" + v + "]") : "Optional.empty";
	}
}
