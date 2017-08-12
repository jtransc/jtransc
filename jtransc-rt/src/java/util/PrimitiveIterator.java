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
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public interface PrimitiveIterator<T, T_CONS> extends Iterator<T> {
	@SuppressWarnings("overloads")
	void forEachRemaining(T_CONS action);

	interface OfInt extends PrimitiveIterator<Integer, IntConsumer> {
		int nextInt();

		default void forEachRemaining(IntConsumer action) {
			while (hasNext()) action.accept(nextInt());
		}

		default Integer next() {
			throw new RuntimeException("Not implemented");
		}

		default void forEachRemaining(Consumer<? super Integer> action) {
			throw new RuntimeException("Not implemented");
		}
	}

	interface OfLong extends PrimitiveIterator<Long, LongConsumer> {
		long nextLong();

		default void forEachRemaining(LongConsumer action) {
			while (hasNext())
				action.accept(nextLong());
		}

		default Long next() {
			throw new RuntimeException("Not implemented");
		}

		default void forEachRemaining(Consumer<? super Long> action) {
			throw new RuntimeException("Not implemented");
		}
	}

	interface OfDouble extends PrimitiveIterator<Double, DoubleConsumer> {
		double nextDouble();

		default void forEachRemaining(DoubleConsumer action) {
			while (hasNext())
				action.accept(nextDouble());
		}

		default Double next() {
			throw new RuntimeException("Not implemented");
		}

		default void forEachRemaining(Consumer<? super Double> action) {
			throw new RuntimeException("Not implemented");
		}
	}
}
