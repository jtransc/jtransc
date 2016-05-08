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

import com.jtransc.ds.flatMapInChunks
import com.jtransc.ds.split
import org.junit.Assert
import org.junit.Test

class CollectionUtilsTest {
	@Test fun test() {
		var log = listOf<String>()
		listOf(1, 2, 3).flatMapInChunks(2) {
			log += "CHUNK($it)"
			it
		}
		Assert.assertEquals("[CHUNK([1, 2]), CHUNK([2, 3])]", log.toString())
	}

	@Test fun test2() {
		var log = listOf<String>()
		listOf(1, 2).flatMapInChunks(2) {
			log += "CHUNK($it)"
			it
		}
		Assert.assertEquals("[CHUNK([1, 2])]", log.toString())
	}

	@Test fun test3() {
		var log = listOf<String>()
		listOf(1, 2, 3, 4, 5).flatMapInChunks(2) {
			log += "CHUNK($it)"
			it.map { -it }
		}
		Assert.assertEquals("[CHUNK([1, 2]), CHUNK([-2, 3]), CHUNK([-3, 4]), CHUNK([-4, 5])]", log.toString())
	}

	@Test fun split() {
		val parts = listOf("a", ":", "b", "c", ":", "d").split(":")
		Assert.assertEquals(listOf(listOf("a"), listOf("b", "c"), listOf("d")), parts)
	}
}