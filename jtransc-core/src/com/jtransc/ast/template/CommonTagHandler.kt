package com.jtransc.ast.template

import com.jtransc.ast.*
import com.jtransc.error.InvalidOperationException
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

	interface Result {
		val ref: AstRef
	}
	data class SINIT(val method: AstMethod) : Result {
		override val ref = method.ref
	}
	data class CONSTRUCTOR(override val ref: AstMethodRef, val method: AstMethod) : Result {
	}
	data class METHOD(override val ref: AstMethodRef, val method: AstMethod, val isStatic: Boolean) : Result {
	}
	data class FIELD(override val ref: AstFieldRef, val field: AstField, val isStatic: Boolean) : Result
	data class CLASS(val clazz: AstClass) : Result {
		override val ref = clazz.ref
	}
	data class CLASS_REF(val clazz: FqName) : Result {
		override val ref = clazz.ref
	}

	private fun resolveClassName(str: String, params: HashMap<String, Any?>): FqName = str.replace('@', '$').fqname

	fun getRef(program: AstProgram, type: String, desc: String, params: HashMap<String, Any?>): Result {
		val dataParts = desc.split(':').map { getOrReplaceVar(it, params) }
		val desc2 = dataParts.joinToString(":")
		val classFqname = resolveClassName(dataParts[0], params)
		if (!program.contains(classFqname)) {
			invalidOp("evalReference: Can't find class '$classFqname' (I)")
		}
		val clazz = program[classFqname]
		val types = program.types
		val tag = type.toUpperCase()

		try {
			return when (tag) {
				"SINIT" -> SINIT(clazz.getMethod("<clinit>", "()V")!!)
				"CONSTRUCTOR" -> {
					if (dataParts.size >= 2) {
						val ref = AstMethodRef(clazz.name, "<init>", types.demangleMethod(dataParts[1]))
						CONSTRUCTOR(ref, program[ref] ?: invalidOp("Can't find ref $ref"))
					} else {
						val methods = clazz.constructors
						if (methods.isEmpty()) invalidOp("evalReference: Can't find constructor $desc2")
						if (methods.size > 1) invalidOp("evalReference: Several signatures for constructor $desc2, please specify signature")
						val method = methods.first()
						CONSTRUCTOR(method.ref, method)
					}
				}
				"SMETHOD", "METHOD" -> {
					val isStatic = (tag == "SMETHOD")
					if (dataParts.size >= 3) {
						val ref = AstMethodRef(clazz.name, dataParts[1], types.demangleMethod(dataParts[2]))
						METHOD(ref, program[ref] ?: invalidOp("Can't find ref $ref"), isStatic)
					} else {
						val methods = clazz.getMethodsInAncestorsAndInterfaces(dataParts[1])
						if (methods.isEmpty()) invalidOp("evalReference: Can't find method $desc2")
						if (methods.size > 1) invalidOp("evalReference: Several signatures, please specify signature")
						val method = methods.first()
						METHOD(method.ref, method, isStatic)
					}
				}
				"SFIELD", "FIELD" -> {
					val field = clazz.locateField(dataParts[1]) ?: invalidOp("evalReference: Can't find field $desc2")
					FIELD(field.ref, field, isStatic = (tag == "SFIELD"))
				}
				"CLASS" -> CLASS(clazz)
				else -> invalidOp("evalReference: Unknown type!")
			}
		} catch (e: Throwable) {
			throw InvalidOperationException("Invalid: $type $desc : ${e.message}", e)
		}
	}

	fun getClassRef(program: AstProgram, type: String, desc: String, params: HashMap<String, Any?>): FqName {
		val dataParts = desc.split(':').map { getOrReplaceVar(it, params) }
		return resolveClassName(dataParts[0], params)
	}
}