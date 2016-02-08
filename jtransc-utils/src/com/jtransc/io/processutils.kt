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

import com.jtransc.vfs.UTF8
import com.jtransc.vfs.UTF8
import java.io.File

data class ProcessResult2(val output: String, val exitValue: Int) {
	val success = exitValue == 0
}

object ProcessUtils {
	fun run(currentDir: File, command: String, args: List<String>, redirect: Boolean): ProcessResult2 {
		val pb = ProcessBuilder(command, *args.toTypedArray());
		pb.directory(currentDir)
		if (redirect) {
			pb.inheritIO()
		}
		val p = pb.start()
		val err = if (redirect) "" else p.inputStream.readBytes().toString(UTF8) + p.errorStream.readBytes().toString(UTF8)
		p.waitFor()
		return ProcessResult2(err, p.exitValue())
	}

	fun runAndReadStderr(currentDir: File, command: String, args: List<String>): ProcessResult2 {
		return run(currentDir, command, args, redirect = false)
	}

	fun runAndRedirect(currentDir: File, command: String, args: List<String>): ProcessResult2 {
		return run(currentDir, command, args, redirect = true)
	}
}
