package com.jtransc.ast.treeshaking

import com.jtransc.ast.AstProgram
import com.jtransc.ast.AstRef
import com.jtransc.ast.FqName
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.template.Minitemplate
import java.util.*

enum class TRefReason {
	UNKNOWN, TREESHAKING, STATIC
}

class TRefConfig(
	val reason: TRefReason = TRefReason.UNKNOWN
)

fun GetTemplateReferencesRefs(program: AstProgram, templateStr: String, currentClass: FqName, config: TRefConfig): List<AstRef> {
	return GetTemplateReferences(program, templateStr, currentClass, config = config).map { it.ref }
}

fun GetTemplateReferences(program: AstProgram, templateStr: String, currentClass: FqName, config: TRefConfig): List<CommonTagHandler.Result> {
	return _GetTemplateReferences(program, templateStr, currentClass, classes = false, config = config)
}

fun GetClassTemplateReferences(program: AstProgram, templateStr: String, currentClass: FqName, config: TRefConfig): List<FqName> {
	return _GetTemplateReferences(program, templateStr, currentClass, classes = true, config = config).map { (it as CommonTagHandler.CLASS_REF).clazz }
}


fun _GetTemplateReferences(program: AstProgram, templateStr: String, currentClass: FqName, classes: Boolean, config: TRefConfig): List<CommonTagHandler.Result> {
	val refs = arrayListOf<CommonTagHandler.Result>()
	val params: HashMap<String, Any?> = hashMapOf("CLASS" to currentClass.fqname)
	val template = Minitemplate(templateStr, Minitemplate.Config(
		extraTags = listOf(
			Minitemplate.Tag(
				":programref:", setOf(), null,
				aliases = CommonTagHandler.ALIASES
			) {
				val tag = it.first().token.name
				val desc = it.first().token.content
				if (classes) {
					refs += CommonTagHandler.CLASS_REF(CommonTagHandler.getClassRef(program, tag, desc, params))
				} else {
					refs += CommonTagHandler.getRef(program, tag, desc, params)
				}

				Minitemplate.BlockNode.TEXT("")
			}
		),
		extraFilters = listOf(
		)
	))
	template(hashMapOf<String, Any?>())
	return refs
}