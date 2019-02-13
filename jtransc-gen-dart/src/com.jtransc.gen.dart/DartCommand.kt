package com.jtransc.gen.dart

import com.jtransc.io.*

object DartCommand {
	val dart = "dart"
	val available get() = ProcessUtils.which(dart) != null
}