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

package all.gen.js

import org.junit.Assert
import org.junit.Test
import javax.script.ScriptEngineManager

class ExecTest {
	val engine = ScriptEngineManager().getEngineByMimeType("text/javascript")
	@Test
	fun testName() {
		Assert.assertEquals(10, engine.eval("(function() { return 10; })()"));
	}

	@Test
	fun testName2() {
		Assert.assertEquals(10, engine.eval("(function() { return 10; })()"));
	}
}