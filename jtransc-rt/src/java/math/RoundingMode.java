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

package java.math;

public enum RoundingMode {
	UP(BigDecimal.ROUND_UP),
	DOWN(BigDecimal.ROUND_DOWN),
	CEILING(BigDecimal.ROUND_CEILING),
	FLOOR(BigDecimal.ROUND_FLOOR),
	HALF_UP(BigDecimal.ROUND_HALF_UP),
	HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),
	HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),
	UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);
	final int oldMode;

	RoundingMode(int oldMode) {
		this.oldMode = oldMode;
	}

	public static RoundingMode valueOf(int rm) {
		switch (rm) {
			case BigDecimal.ROUND_UP:
				return UP;

			case BigDecimal.ROUND_DOWN:
				return DOWN;

			case BigDecimal.ROUND_CEILING:
				return CEILING;

			case BigDecimal.ROUND_FLOOR:
				return FLOOR;

			case BigDecimal.ROUND_HALF_UP:
				return HALF_UP;

			case BigDecimal.ROUND_HALF_DOWN:
				return HALF_DOWN;

			case BigDecimal.ROUND_HALF_EVEN:
				return HALF_EVEN;

			case BigDecimal.ROUND_UNNECESSARY:
				return UNNECESSARY;

			default:
				throw new IllegalArgumentException("argument out of range");
		}
	}
}
