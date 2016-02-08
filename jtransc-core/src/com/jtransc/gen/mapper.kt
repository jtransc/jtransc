/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.gen

import com.jtransc.ast.*

class ClassMappings {
	private val mappings = hashMapOf<String, ClassMapping>()
	val adaptorsSet = hashSetOf<String>()

	fun map(className: String, callback: ClassMapping.() -> Unit) {
		mappings[className] = ClassMapping.map(this, className, callback)
	}

	fun getClassMapping(clazz: String): ClassMapping? = mappings[clazz]
	fun getClassMapping(clazz: FqName): ClassMapping? = mappings[clazz.fqname]
	fun getClassMapping(clazz: AstClass): ClassMapping? = mappings[clazz.name.fqname]
	fun getClassMapping(clazz: AstClassRef): ClassMapping? = mappings[clazz.name.fqname]
	fun getClassMapping(clazz: AstType.REF): ClassMapping? = mappings[clazz.name.fqname]

	fun getBody(method: AstMethodRef): String? {
		val mapping = mappings[method.containingClass.fqname]
		if (mapping != null) {
			return mapping.functionBodies[method.fid] ?: mapping.functionBodies[method.fidWildcard]
		}
		return null
	}

	fun hasClassReplacement(fqname: FqName): Boolean = mappings[fqname.fqname]?.classReplacement != null
	fun getClassReplacement(fqname: FqName) = mappings[fqname.fqname]?.classReplacement
	fun isAdaptorSet(fqname: String): Boolean = fqname in adaptorsSet

	fun isFieldAvailable(field: AstFieldRef): Boolean {
		return !(getClassMapping(field.containingClass)?.removeFields?.contains(field.name) ?: false)
	}

	fun getClassAdaptor(from: AstType, to: AstType): ClassMapping.ClassAdaptor? {
		if ((from is AstType.REF) && (to is AstType.REF)) {
			val mapping = mappings[from.name.fqname]
			if (mapping != null) {
				return mapping.classAdaptors[to.name.fqname]
			}
		}
		return null
	}

	fun getFunctionInline(method: AstMethodRef): ClassMapping.FunctionReplacement? {
		val mapping = mappings[method.containingClass.fqname]
		if (mapping != null) {
			return mapping.functionInlines[method.fid] ?: mapping.functionInlines[method.fidWildcard]
		}
		return null
	}
}

class ClassMapping(val mappings: ClassMappings, val className: String) {
	data class ClassReplacement(val base: String, val importNew: String, val typeTag: String)
	data class ClassAdaptor(val from: String, val to: String, val adaptor: String)
	data class FunctionReplacement(val replacement: String, val imports: List<String>)
	data class ConstructorReplacement(val replacement: String, val imports: List<String>)

	companion object {
		fun map(mappings: ClassMappings, className: String, callback: ClassMapping.() -> Unit): ClassMapping {
			val mapper = ClassMapping(mappings, className)
			mapper.callback()
			return mapper
		}
	}

	val CLASS = AstType.CLASS
	val OBJECT = AstType.OBJECT
	val STRING = AstType.STRING
	val BOOL = AstType.BOOL
	val VOID = AstType.VOID
	val BYTE = AstType.BYTE
	val CHAR = AstType.CHAR
	val SHORT = AstType.SHORT
	val INT = AstType.INT
	val LONG = AstType.LONG
	val FLOAT = AstType.FLOAT
	val DOUBLE = AstType.DOUBLE

	fun REF(name:String) = AstType.REF(name)
	fun ARRAY(type: AstType) = AstType.ARRAY(type)
	fun METHOD(ret: AstType, vararg args: AstType): String = AstType.METHOD_TYPE(args.toList().toArguments(), ret).desc
	fun ARGS(vararg types: AstType) = listOf(*types)

	fun nativeImport(import: String) {
		nativeImports.add(import)
	}

	fun nativeMember(str: String) {
		nativeMembers.add(str)
	}

	fun removeField(name: String) {
		removeFields.add(name)
	}

	fun body(method: String, desc: String, replacement: String) {
		val fid = "$className:$method:$desc"
		functionBodies[fid] = replacement
	}


	fun body(retval: AstType, method: String, args: List<AstType>, replacement: String) {
		body(method, METHOD(retval, *args.toTypedArray()), replacement);
	}

	@Deprecated("Use body instead")
	fun call(retval: AstType, method: String, args: List<AstType>, replacement: String) {
		call(method, METHOD(retval, *args.toTypedArray()), replacement);
	}

	@Deprecated("Use body instead")
	fun callConstructor(args: List<AstType>, replacement: String) {
		call(VOID, "<init>", args, replacement)
	}

	@Deprecated("Use body instead")
	fun call(method: String, desc: String, replacement: String, imports: List<String> = listOf()) {
		val fid = "$className:$method:$desc"
		functionInlines[fid] = FunctionReplacement(replacement, imports)
	}

	@Deprecated("Use body instead")
	fun adaptor(expectedType: String, adaptor: String) {
		classAdaptors[expectedType] = ClassAdaptor(className, expectedType, adaptor)
		mappings.adaptorsSet.add(adaptor)
	}

	@Deprecated("Use body instead")
	fun classReplacement(importNew: String, typeTag: String = importNew) {
		this.classReplacement = ClassReplacement(className, importNew, typeTag)
	}

	val nativeImports = arrayListOf<String>()
	val nativeMembers = arrayListOf<String>()
	val removeFields = hashSetOf<String>()
	var classReplacement: ClassReplacement? = null
	val functionInlines = hashMapOf<String, FunctionReplacement>()
	val classAdaptors = hashMapOf<String, ClassAdaptor>()
	val functionBodies = hashMapOf<String, String>()
}
