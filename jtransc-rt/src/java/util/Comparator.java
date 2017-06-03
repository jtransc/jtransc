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

import java.io.Serializable;

public interface Comparator<T> {
	int compare(T lhs, T rhs);

	boolean equals(Object object);

	default Comparator<T> reversed() {
		return Collections.reverseOrder(this);
	}

	// @TODO: Not supported DynamicInvoke yet!
	//default Comparator<T> thenComparing(Comparator<? super T> other) {
	//	return (Comparator<T> & Serializable) (c1, c2) -> {
	//		int res = compare(c1, c2);
	//		return (res != 0) ? res : other.compare(c1, c2);
	//	};
	//}

	default <T extends Comparable<? super T>> Comparator<T> reverseOrder() {
		return Collections.reverseOrder();
	}
}