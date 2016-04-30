package com.jtransc.intellij.plugin

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpointType

class JTranscBreakpointType : XLineBreakpointType<XBreakpointProperties<Any?>>("JTransc", "JTranscDebugger") {
	override fun createBreakpointProperties(file: VirtualFile, line: Int): XBreakpointProperties<Any?>? {
		return null
	}

	override fun getEnabledIcon() = JTranscIcons.ICON
}