package com.jtransc.plugin.finalizer

import com.jtransc.ast.AstClass
import com.jtransc.ast.AstModifiers
import com.jtransc.ast.AstProgram
import com.jtransc.lang.extraProperty
import com.jtransc.plugin.JTranscPlugin

var AstClass.descendantList by extraProperty { arrayListOf<AstClass>() }
val AstClass.descendantCount get() = descendantList.size

class FinalizerJTranscPlugin : JTranscPlugin() {
	override fun processAfterTreeShaking(program: AstProgram) {
		for (clazz in program.classes) {
			for (related in clazz.getAllRelatedTypes()) {
				if (related == clazz) continue
				related.descendantList.add(related)
			}
		}
		for (clazz in program.classes) {
			if (clazz.descendantCount == 0) {
				clazz.modifiers.acc = clazz.modifiers.acc or AstModifiers.ACC_FINAL
			}
		}
	}
}