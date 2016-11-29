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

package com.jtransc.env

import com.jtransc.vfs.LocalAndJars
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File

class RuntimeProvider {
	private val cl = javaClass.classLoader
	private val currentClassPath = javaClass.name.replace(".", "/") + ".class"
	//println(currentClassPath)
	private val javaAotProjectDirectory = File(cl.getResource(currentClassPath).path).parentFile.parentFile.parentFile
	private val javaAotProjectPath = javaAotProjectDirectory.absolutePath

	//val project_root = Paths.get("$javaAotProjectPath/../..").normalize().toFile().absolutePath
	val project_root = File("$javaAotProjectPath/../../../").canonicalPath
	val java_runtime_classes_path = "$project_root/jtransc-rt/target/classes"
	//val java_runtime_classes_path = "$javaAotProjectPath"

	//val java_sample1_classes_path = "$project_root/out_sample1"
	val java_sample1_classes_path = "$javaAotProjectPath"

	var runtimeClassesVfs = MergeVfs(listOf(LocalVfs(File(java_runtime_classes_path))))
	var classpaths = listOf(java_runtime_classes_path)

	fun setClassPaths(paths: List<String>): Unit {
		classpaths = paths

		runtimeClassesVfs = MergeVfs(listOf(LocalVfs(File(java_runtime_classes_path))) + LocalAndJars(paths))
	}

	fun getClassVfsNode(className: String): SyncVfsFile {
		val file = runtimeClassesVfs[className.replace('.', '/') + ".class"]
		//println(file.exists)
		if (!file.exists) {
			println("Not exists!")
		}
		return file
	}
}
