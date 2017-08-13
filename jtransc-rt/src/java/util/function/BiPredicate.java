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

package java.util.function;

@FunctionalInterface
public interface BiPredicate<T, U> {
	boolean test(T left, U right);

	default BiPredicate<T, U> and(BiPredicate<? super T, ? super U> that) {
		return (T left, U right) -> test(left, right) && that.test(left, right);
	}

	default BiPredicate<T, U> negate() {
		return (T left, U right) -> !test(left, right);
	}

	default BiPredicate<T, U> or(BiPredicate<? super T, ? super U> that) {
		return (T left, U right) -> test(left, right) || that.test(left, right);
	}
}
