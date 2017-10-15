package com.jtransc.ast.async

import com.jtransc.annotation.JTranscSync
import com.jtransc.ast.AstMethod
import com.jtransc.ast.contains
import com.jtransc.ast.isAbstract
import com.jtransc.gen.TargetName
import com.jtransc.lang.extraProperty
import com.jtransc.lang.weakExtra

class AsyncAnalyzer(val target: TargetName) {
	/*
	var AstMethod.isComputingAsync by weakExtra { false }
	var AstMethod.isAsync: Boolean? by weakExtra { null }

	fun isMethodAsync(m: AstMethod): Boolean? {
		if (m.isComputingAsync) return null
		m.isComputingAsync = true

		if (m.isAsync == null) {
			if (m.annotationsList.getBodiesForTarget(target).any { it.async }) {
				m.isAsync = true
			} else {

			}

			for (cmethod in m.bodyDependencies.methods) {

			}
		}
		return m.isAsync!!
	}
	*/

	var AstMethod.isAsync: Boolean? by extraProperty { null }

	fun isMethodAsync(m: AstMethod): Boolean {
		if (m.isAsync == null) {
			m.isAsync = isMethodAsyncUncached(m)
		}
		return m.isAsync!!
	}

	fun isMethodAsyncUncached(m: AstMethod): Boolean {
		// @TODO: Implement this!

		if (m.asyncOpt != null) return m.asyncOpt!!

		// If this method is overriding other method, or has overrides or it is implementing an interface, we have
		// to check all those methods and if any one of them is asynchronous, we have to propagate here too.

		// Also when it has ENTERMONITOR and/or EXITMONITOR opcodes

		// When calling virtual/interface methods we have to find all the possible methods and if one of them
		// is asynchronous this should be stickily asynchronous too.

		// Note: since toString would probably end being async in this case, we should try hard to devirtualize
		// as much as we can to avoid asynchronous paths.

		if (m.annotationsList.contains<JTranscSync>()) {
			return false
		}

		// No method calls and has body, so probably no method calls.
		//if (m.hasBody && m.bodyDependencies.methods.isEmpty()) return false

		// Not calling other methods, so must be synchronous!
		return true
	}
}

