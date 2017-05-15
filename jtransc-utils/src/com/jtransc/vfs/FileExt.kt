package com.jtransc.vfs

import java.io.File

operator fun File.get(name: String) = File(this, name)