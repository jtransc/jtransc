package com.jtransc.ast.feature.method

import com.jtransc.ast.*

class UndeterministicParameterEvaluationFeature : AstMethodFeature() {
	override fun add(method: AstMethod, body: AstBody, settings: AstBuildSettings, types: AstTypes): AstBody {
		// @TODO: This should change AST to a TIR or similar
		return body
	}
}