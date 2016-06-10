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

package com.jtransc.io

import com.jtransc.vfs.ProcessResult
import com.jtransc.vfs.UTF8
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

data class ProcessResult2(val exitValue: Int, val out:String = "", val err:String = "", val outerr: String = out + err) {
	val success = exitValue == 0

	constructor(pr: ProcessResult) : this(pr.exitCode, pr.outputString, pr.errorString, pr.outputString + pr.errorString)
}

object ProcessUtils {
	fun run(currentDir: File, command: String, args: List<String>, redirect: Boolean, env: Map<String, String> = mapOf()): ProcessResult2 {
		var out = ""
		var err = ""
		var outerr = ""
		val exitValue = run2(currentDir, command, args, object : ProcessHandler() {
			override fun onStarted() {
			}

			override fun onOutputData(data: String) {
				if (redirect) System.out.print(data)
				out += data
				outerr += data
			}

			override fun onErrorData(data: String) {
				if (redirect) System.err.print(data)
				err += data
				outerr += data
			}

			override fun onCompleted(exitValue: Int) {
			}
		}, env = env)

		return ProcessResult2(exitValue, out, err, outerr)
	}

	fun runAndReadStderr(currentDir: File, command: String, args: List<String>, env:Map<String, String> = mapOf()): ProcessResult2 {
		return run(currentDir, command, args, redirect = false, env = env)
	}

	fun runAndRedirect(currentDir: File, command: String, args: List<String>, env:Map<String, String> = mapOf()): ProcessResult2 {
		return run(currentDir, command, args, redirect = true, env = env)
	}

	fun runAndRedirect(currentDir: File, commandAndArgs: List<String>, env:Map<String, String> = mapOf()): ProcessResult2 {
		return run(currentDir, commandAndArgs.first(), commandAndArgs.drop(1), redirect = true, env = env)
	}

	open class ProcessHandler(val parent: ProcessHandler? = null) {
		open fun onStarted(): Unit = parent?.onStarted() ?: Unit
		open fun onOutputData(data: String): Unit = parent?.onOutputData(data) ?: Unit
		open fun onErrorData(data: String): Unit = parent?.onErrorData(data) ?: Unit
		open fun onCompleted(exitValue: Int): Unit = parent?.onCompleted(exitValue) ?: Unit
	}

	object RedirectOutputHandler : ProcessHandler() {
		override fun onOutputData(data: String) = System.out.print(data)
		override fun onErrorData(data: String) = System.err.print(data)
		override fun onCompleted(exitValue: Int) = Unit
	}

	fun run2(currentDir: File, command: String, args: List<String>, handler: ProcessHandler = RedirectOutputHandler, charset: Charset = Charsets.UTF_8, env: Map<String, String> = mapOf()): Int {
		val pb = ProcessBuilder(command, *args.toTypedArray());
		pb.directory(currentDir)
		val penv = pb.environment()
		for ((key, value) in env.entries) {
			if (key.startsWith("*")) {
				val akey = key.substring(1)
				penv[akey] = value + penv[akey]
			} else if (key.endsWith("*")) {
				val akey = key.substring(0, key.length - 1)
				penv[akey] = penv[akey] + value
			} else {
				penv[key] = value
			}
		}
		val p = pb.start()
		val input = p.inputStream
		val error = p.errorStream
		while (true) {
			val i = input.readAvailableChunk()
			val e = error.readAvailableChunk()
			if (i.size > 0) handler.onOutputData(i.toString(charset))
			if (e.size > 0) handler.onErrorData(e.toString(charset))
			if (i.size == 0 && e.size == 0 && !p.isAlive) break
			Thread.sleep(1L)
		}
		p.waitFor()
		handler.onCompleted(p.exitValue())
		return p.exitValue()
	}

	fun runAsync(currentDir: File, command: String, args: List<String>, handler: ProcessHandler = RedirectOutputHandler, charset: Charset = Charsets.UTF_8) {
		Thread {
			run2(currentDir, command, args, handler, charset)
		}.start()
	}
}

