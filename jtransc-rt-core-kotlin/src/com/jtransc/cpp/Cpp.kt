package com.jtransc.cpp

import com.jtransc.annotation.JTranscCallSiteBody
import com.jtransc.annotation.JTranscLiteralParam

object Cpp {
	@JTranscCallSiteBody(target = "cs", value = "#0")
	@JvmStatic external fun v_raw(@JTranscLiteralParam str: String): Unit

	@JTranscCallSiteBody(target = "cs", value = "#0")
	@JvmStatic external fun b_raw(@JTranscLiteralParam str: String): Boolean

	@JTranscCallSiteBody(target = "cs", value = "#0")
	@JvmStatic external fun i_raw(@JTranscLiteralParam str: String): Int

	@JTranscCallSiteBody(target = "cs", value = "#0")
	@JvmStatic external fun f_raw(@JTranscLiteralParam str: String): Float

	@JTranscCallSiteBody(target = "cs", value = "#0")
	@JvmStatic external fun d_raw(@JTranscLiteralParam str: String): Double

	@JTranscCallSiteBody(target = "cs", value = "#0")
	@JvmStatic external fun l_raw(@JTranscLiteralParam str: String): Long

	@JTranscCallSiteBody(target = "cs", value = "#0")
	@JvmStatic external fun <T> raw(@JTranscLiteralParam str: String): T
}