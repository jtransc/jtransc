package com.jtransc.ast.treeshaking

import com.jtransc.annotation.JTranscAddFile
import com.jtransc.ast.AstAnnotationList

fun AstAnnotationList.getTargetAddFiles(target: String): List<JTranscAddFile> {
	return getTypedList(com.jtransc.annotation.JTranscAddFileList::value).filter { it.target == "all" || it.target == target }
}

fun JTranscAddFile.filesToProcess(): List<String> {
	return if (this.process) {
		listOf(this.prepend, this.prependAppend, this.append)
	} else {
		listOf()
	}.filter { it.isNotEmpty() }
}