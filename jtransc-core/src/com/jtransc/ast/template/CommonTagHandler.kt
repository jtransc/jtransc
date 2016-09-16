package com.jtransc.ast.template

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import java.util.*

object CommonTagHandler {
	private fun getOrReplaceVar(name: String, params: HashMap<String, Any?>): String {
		val out = if (name.startsWith("#")) {
			params[name.substring(1)].toString()
		} else {
			name
		}
		return out
	}

	interface Result
	data class SINIT(val method: AstMethod) : Result
	data class CONSTRUCTOR(val method: AstMethod) : Result
	data class METHOD(val method: AstMethod, val isStatic: Boolean) : Result
	data class FIELD(val field: AstField, val isStatic: Boolean) : Result
	data class CLASS(val clazz: AstClass) : Result

	fun getRefFqName(desc: String, params: HashMap<String, Any?>): FqName {
		val dataParts = desc.split(':').map { getOrReplaceVar(it, params) }
		val desc2 = dataParts.joinToString(":")
		return dataParts[0].replace('@', '$').fqname
	}

	fun getRef(program: AstProgram, type: String, desc: String, params: HashMap<String, Any?>): Result {
		val dataParts = desc.split(':').map { getOrReplaceVar(it, params) }
		val desc2 = dataParts.joinToString(":")
		val classFqname = dataParts[0].replace('@', '$').fqname
		if (!program.contains(classFqname)) invalidOp("evalReference: Can't find class $classFqname (I)")
		val clazz = program[classFqname]
		val types = program.types
		val tag = type.toUpperCase()

		try {
			return when (tag) {
				"SINIT" -> SINIT(clazz.getMethod("<clinit>", "()V")!!)
				"CONSTRUCTOR" -> CONSTRUCTOR(program[AstMethodRef(clazz.name, "<init>", types.demangleMethod(dataParts[1]))]!!)
				"SMETHOD", "METHOD" -> {
					METHOD(if (dataParts.size >= 3) {
						program[AstMethodRef(clazz.name, dataParts[1], types.demangleMethod(dataParts[2]))]!!
					} else {
						val methods = clazz.getMethodsInAncestorsAndInterfaces(dataParts[1])
						if (methods.isEmpty()) invalidOp("evalReference: Can't find method $desc2")
						if (methods.size > 1) invalidOp("evalReference: Several signatures, please specify signature")
						methods.first()
					}, isStatic = (tag == "SMETHOD"))
				}
				"SFIELD", "FIELD" -> FIELD(clazz.locateField(dataParts[1]) ?: invalidOp("evalReference: Can't find field $desc2"), isStatic = (tag == "SFIELD"))
				"CLASS" -> CLASS(clazz)
				else -> invalidOp("evalReference: Unknown type!")
			}
		} catch (e: Throwable) {
			throw RuntimeException("Invalid: $type $desc", e)
		}
	}
}