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

import com.jtransc.JTranscSystem
import com.jtransc.ds.toHashMap
import com.jtransc.env.OS
import com.jtransc.error.invalidOp
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.ProcessResult
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset


data class ProcessResult2(val exitValue: Int, val out: String = "", val err: String = "", val outerr: String = out + err) {
	val success = exitValue == 0

	constructor(pr: ProcessResult) : this(pr.exitCode, pr.outputString, pr.errorString, pr.outputString + pr.errorString)
}

object ProcessUtils {
	//val defaultCharset = Charset.forName("UTF-8")
	val defaultCharset = if (OS.isWindows) Charset.forName("UTF-8") else Charset.defaultCharset()
	//val defaultCharset = Charset.forName("ISO-8859-1")
	//val defaultCharset = Charset.forName("ASCII")
	//val defaultCharset = Charset.forName("Cp850")
	//val defaultCharset = Charset.forName("Cp1252")

	//init {
	//	println(getConsoleCharset())
	//}
	//
	//private fun getConsoleCharset(): String {
	//	val osw = OutputStreamWriter(System.out)
	//	println(osw.encoding)
	//	//val stdOutClass = System.out.javaClass
	//	////val charOutField = stdOutClass.getDeclaredField("charOut")
	//	//val charOutField = stdOutClass.getField("charOut")
	//	//charOutField.isAccessible = true
	//	//val o = charOutField.get(System.out) as OutputStreamWriter
	//	//System.out.println(o.getEncoding())
	//	return osw.encoding
	//}

	fun run(currentDir: File, command: String, args: List<String>, options: ExecOptions): ProcessResult2 {
		//val pb = ProcessBuilder("cmd.exe", "/c", "chcp", "65001").inheritIO()
		//val p = pb.start()
		//p.waitFor()


		var out = ""
		var err = ""
		var outerr = ""
		//redirect: Boolean, env: Map<String, String> = mapOf()
		val exitValue = run2(currentDir, command, args, object : ProcessHandler() {
			override fun onStarted() {
			}

			override fun onOutputData(data: String) {
				if (options.redirect) System.out.print(data)
				out += data
				outerr += data
			}

			override fun onErrorData(data: String) {
				if (options.redirect) System.err.print(data)
				err += data
				outerr += data
			}

			override fun onCompleted(exitValue: Int) {
			}
		}, options = options)

		return ProcessResult2(exitValue, out, err, outerr)
	}

	fun runAndReadStderr(currentDir: File, command: String, args: List<String>, env: Map<String, String> = mapOf()): ProcessResult2 {
		return run(currentDir, command, args, options = ExecOptions().copy(passthru = false, env = env))
	}

	fun runAndRedirect(currentDir: File, command: String, args: List<String>, env: Map<String, String> = mapOf()): ProcessResult2 {
		return run(currentDir, command, args, options = ExecOptions().copy(passthru = true, env = env))
	}

	fun runAndRedirect(currentDir: File, commandAndArgs: List<String>, env: Map<String, String> = mapOf()): ProcessResult2 {
		return run(currentDir, commandAndArgs.first(), commandAndArgs.drop(1), options = ExecOptions().copy(passthru = true, env = env))
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

	val pathSeparator by lazy { System.getProperty("path.separator") ?: ":" }
	val fileSeparator by lazy { System.getProperty("file.separator") ?: "/" }

	fun getPaths(): List<String> {
		val env = System.getenv("PATH") ?: ""
		return env.split(pathSeparator)
	}

	fun locateCommandSure(name: String): String = locateCommand(name) ?: invalidOp("Can't find command $name in path")

	fun which(name: String): String? = locateCommand(name)

	fun locateCommand(name: String): String? {
		for (ext in if (JTranscSystem.isWindows()) listOf(".exe", ".cmd", ".bat", "") else listOf("")) {
			for (path in getPaths()) {
				val fullPath = "$path$fileSeparator$name$ext"
				if (File(fullPath).exists()) return fullPath
			}
		}
		return null
	}

	fun run2(currentDir: File, command: String, args: List<String>, handler: ProcessHandler = RedirectOutputHandler, charset: Charset = defaultCharset, options: ExecOptions = ExecOptions()): Int {
		val fullCommand = if (File(command).isAbsolute) command else locateCommandSure(command)

		val absoluteCurrentDir = currentDir.absoluteFile
		val env = options.env

		val penv = System.getenv().toHashMap()
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

		val p = if (options.sysexec) {
			val envList = penv.map { it.key + "=" + it.value }
			//val envList = listOf<String>()

			val prefixCmds = if (OS.isWindows) listOf("cmd", "/c", "chcp", "65001", ">", "NUL", "&") else listOf()

			Runtime.getRuntime().exec((prefixCmds + listOf(fullCommand) + args).toTypedArray(), envList.toTypedArray(), absoluteCurrentDir)
		} else {
			val pb = ProcessBuilder(fullCommand, *args.toTypedArray());
			pb.directory(absoluteCurrentDir)
			val penv2 = pb.environment()
			for ((key, value) in penv.entries) penv2[key] = value
			pb.start()
		}
		val input = InputStreamReader(p.inputStream, charset)
		val error = InputStreamReader(p.errorStream, charset)
		var closing = false
		while (true) {
			val i = input.readAvailableChunk(p.inputStream, readRest = closing)
			val e = error.readAvailableChunk(p.errorStream, readRest = closing)
			if (i.isNotEmpty()) handler.onOutputData(i)
			if (e.isNotEmpty()) handler.onErrorData(e)
			if (closing) break
			if (i.isEmpty() && e.isEmpty() && !p.isAliveJre7) {
				closing = true
				continue
			}
			Thread.sleep(1L)
		}
		p.waitFor()
		handler.onCompleted(p.exitValue())
		return p.exitValue()
	}

	//fun runAsync(currentDir: File, command: String, args: List<String>, handler: ProcessHandler = RedirectOutputHandler, charset: Charset = Charsets.UTF_8) {
	fun runAsync(currentDir: File, command: String, args: List<String>, handler: ProcessHandler = RedirectOutputHandler, charset: Charset = defaultCharset) {
		Thread {
			run2(currentDir, command, args, handler, charset)
		}.start()
	}
}

private fun InputStreamReader.readAvailableChunk(i: InputStream, readRest: Boolean): String {
	val out = StringBuilder()
	while (if (readRest) true else i.available() > 0) {
		val c = this.read()
		if (c < 0) break
		out.append(c.toChar())
	}
	return out.toString()
}

val Process.isAliveJre7: Boolean get() = try {
	exitValue()
	false
} catch (e: IllegalThreadStateException) {
	true
}

