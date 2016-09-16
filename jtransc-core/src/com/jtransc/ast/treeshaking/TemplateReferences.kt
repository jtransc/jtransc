package com.jtransc.ast.treeshaking

import com.jtransc.ast.AstProgram
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.template.Minitemplate

fun GetTemplateReferences(program: AstProgram, templateStr: String): List<CommonTagHandler.Result> {
	val refs = arrayListOf<CommonTagHandler.Result>()
	val template = Minitemplate(templateStr, Minitemplate.Config(
		extraTags = listOf(
			Minitemplate.Tag(
				":programref:", setOf(), null,
				aliases = listOf(
					//"sinit", "constructor", "smethod", "method", "sfield", "field", "class",
					"SINIT", "CONSTRUCTOR", "SMETHOD", "METHOD", "SFIELD", "FIELD", "CLASS"
				)
			) {
				val tag = it.first().token.name
				val desc = it.first().token.content
				val ref = CommonTagHandler.getRef(program, tag, desc, hashMapOf())

				refs += ref

				Minitemplate.BlockNode.TEXT("")
			}
		),
		extraFilters = listOf(
		)
	))
	template(hashMapOf<String, Any?>())
	return refs
}