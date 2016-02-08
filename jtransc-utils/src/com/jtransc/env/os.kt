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

object OS {
    private val OS = System.getProperty("os.name").toLowerCase()
    val tempDir by lazy { System.getProperty("java.io.tmpdir") }
    val fileSeparator by lazy { System.getProperty("file.separator") }

    val isWindows:Boolean get() = OS.indexOf("win") >= 0
    val isMac:Boolean get()  = OS.indexOf("mac") >= 0
    val isUnix:Boolean get()  = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0
    val isSolaris:Boolean get()  = OS.indexOf("sunos") >= 0
}
