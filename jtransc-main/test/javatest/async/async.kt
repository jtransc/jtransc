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

package javatest.async

/**
 * Created by mike on 10/11/15.
 */
object AsyncTest {

    @JvmStatic fun main(args: Array<String>) {
        println(sum(3, 5))
        (1 .. 10)
            .filter {
                println("Filter: $it")
                asyncInteger(it) % 2 == 0
            }
            .map {
                println("Map to double: $it")
                sum(it, it)
            }
            .drop(1)
            .forEach {
                println("For each: $it")
            }
    }

    private fun sum(a: Int, b: Int): Int {
        return asyncInteger(a) + asyncInteger(b)
    }

    external fun asyncInteger(n: Int): Int

}