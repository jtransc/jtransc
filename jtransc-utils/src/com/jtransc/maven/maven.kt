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

package com.jtransc.maven

object MavenLocalRepository {
	@JvmStatic fun locateJars(vararg ids:String) = ids.flatMap { locateJars(it) }
	@JvmStatic fun locateJars(ids:List<String>) = ids.flatMap { locateJars(it) }

	@JvmStatic fun locateJars(id:String):List<String> {
		val parts = id.split(":")
		return locateJars(parts[0], parts[1], parts[2])
	}

	@JvmStatic fun locateJars(groupId:String, artifactId:String, version:String):List<String> {
		val userDir = System.getProperty("user.home");
		val groupPath = groupId.replace('.', '/')
		return listOf("$userDir/.m2/repository/$groupPath/$artifactId/$version/$artifactId-$version.jar")
	}
}