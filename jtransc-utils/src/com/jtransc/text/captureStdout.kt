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

package com.jtransc.text

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream

object HookedOutputStream : OutputStream() {
	val originalOut = System.out

	private val capturingPerThread = ThreadLocal<ByteArrayOutputStream>()

	override fun write(b: Int) {
		val baos = capturingPerThread.get()
		if (baos != null) {
			baos.write(b)
		} else {
			originalOut.write(b)
		}
	}

	override fun write(b: ByteArray?, off: Int, len: Int) {
		val baos = capturingPerThread.get()
		if (baos != null) {
			baos.write(b, off, len)
		} else {
			originalOut.write(b, off, len)
		}
	}

	fun captureThreadSafe(callback: () -> Unit): ByteArrayOutputStream {
		val out = ByteArrayOutputStream()
		capturingPerThread.set(out)
		callback()
		capturingPerThread.set(null)
		return out
	}
}

object HookedPrintStream : PrintStream(HookedOutputStream) {
	val os = this.out
}

fun captureStdout(callback: () -> Unit): String {
	if (System.out != HookedPrintStream) System.setOut(HookedPrintStream)

	return HookedOutputStream.captureThreadSafe(callback).toString()

	/*
	val baos = ByteArrayOutputStream();
	val ps = PrintStream(baos);

	val old = System.out;
	try {
		System.setOut(ps);
		callback()
		System.out.flush();
	} finally {
		System.setOut(old);
	}
	return baos.toString()
	*/
}