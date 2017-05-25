package com.jtransc.util

import com.jtransc.io.ra.RAByteArray
import com.jtransc.io.ra.RAFile
import com.jtransc.io.ra.RAStream
import java.io.File

fun ByteArray.open() = RAByteArray(this)
fun File.open() = RAFile(this)
