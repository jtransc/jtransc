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

package com.jtransc.input.soot

import com.jtransc.ast.AstMethodRef
import com.jtransc.ast.AstType
import com.jtransc.ast.FqName
import com.jtransc.env.OS
import soot.*
import soot.options.Options
import soot.tagkit.AnnotationElem
import soot.tagkit.AnnotationStringElem
import soot.tagkit.Tag
import soot.tagkit.VisibilityAnnotationTag
import java.io.File

object SootUtils {
	fun init(classPaths: List<String>): Unit {
		G.reset()

		val Options_v = Options.v()

		//Options.v().set_output_format(Options.output_format_jimple)
		Options.v().set_output_format(Options.output_format_shimple)
		//Options.v().set_output_format(Options.output_format_grimple)
		Options_v.set_include_all(true)
		Options_v.set_print_tags_in_output(true)

		Options_v.set_allow_phantom_refs(false)
		//Options_v.set_allow_phantom_refs(true)

		Options_v.set_keep_line_number(true)

		val file_separator = OS.fileSeparator

		println("file_separator: $file_separator ... PathSeparator: ${File.pathSeparator}")

		Options_v.set_soot_classpath(classPaths.joinToString(File.pathSeparator))

		Options_v.setPhaseOption("jb.dae", "enabled:false")
		Options_v.setPhaseOption("jb.uce", "enabled:false")
		Options_v.setPhaseOption("jap.npc", "enabled:true")
		Options_v.setPhaseOption("jap.abc", "enabled:true")
		Options_v.setPhaseOption("jop", "enabled:true")
		Options_v.setPhaseOption("jop.cse", "enabled:false")
		Options_v.setPhaseOption("jop.bcm", "enabled:false")
		Options_v.setPhaseOption("jop.lcm", "enabled:false")
		Options_v.setPhaseOption("jop.cp", "enabled:false")
		Options_v.setPhaseOption("jop.cpf", "enabled:false")
		Options_v.setPhaseOption("jop.cbf", "enabled:false")
		Options_v.setPhaseOption("jop.dae", "enabled:false")
		Options_v.setPhaseOption("jop.nce", "enabled:false")
		Options_v.setPhaseOption("jop.uce1", "enabled:false")
		Options_v.setPhaseOption("jop.ubf1", "enabled:false")
		Options_v.setPhaseOption("jop.uce2", "enabled:false")
		Options_v.setPhaseOption("jop.ubf2", "enabled:false")
		Options_v.setPhaseOption("jop.ule", "enabled:false")
		Scene.v().loadNecessaryClasses()
	}

	// SootUtils.getTag(method.tags, "Llibcore/MethodBody;", "value") as String?
}

fun SootMethod.getAnnotation(annotationClass: String, fieldName: String): Any? = this.tags.getAnnotation(annotationClass, fieldName)
fun SootField.getAnnotation(annotationClass: String, fieldName: String): Any? = this.tags.getAnnotation(annotationClass, fieldName)
fun SootClass.getAnnotation(annotationClass: String, fieldName: String): Any? = this.tags.getAnnotation(annotationClass, fieldName)

fun SootMethod.hasAnnotation(annotationClass: String): Boolean = this.tags.hasAnnotation(annotationClass)

fun Iterable<Tag>.hasAnnotation(annotationClass: String): Boolean {
	val annotationClassType = "L" + annotationClass.replace('.', '/') + ";"
	return this.filterIsInstance<VisibilityAnnotationTag>().flatMap { it.annotations }.any { it.type == annotationClassType }
}

fun Iterable<Tag>.getAnnotation(annotationClass: String, fieldName: String): Any? {
	val annotationClassType = "L" + annotationClass.replace('.', '/') + ";"
	for (at in this.filterIsInstance<VisibilityAnnotationTag>()) {
		for (annotation in at.annotations.filter { it.type == annotationClassType }) {
			for (el in (0 until annotation.numElems).map { annotation.getElemAt(it) }.filter { it.name == fieldName }) {
				fun parseAnnotationElement(el: AnnotationElem): Any? = when (el) {
					is AnnotationStringElem -> el.value
					else -> null
				}

				return parseAnnotationElement(el)
			}
		}
	}
	return null
}

fun SootClass.getSuperClassOrNull(): SootClass? = if (this.hasSuperclass()) this.superclass else null

fun SootClass.getAncestors(includeThis: Boolean = false): List<SootClass> {
	val buffer = arrayListOf<SootClass>()
	var tclazz = if (includeThis) this else this.getSuperClassOrNull()
	while (tclazz != null) {
		buffer.add(tclazz)
		tclazz = tclazz.getSuperClassOrNull()
	}
	return buffer.toList()
}

fun SootClass.hasMethod(method: SootMethod): Boolean = this.hasMethod(method.name, method.parameterTypes as List<Type>)
fun SootClass.hasMethod(name: String, parameterTypes: List<soot.Type>): Boolean {
	/*
	return try {
		this.getMethod(name, parameterTypes) != null
	} catch (e: Throwable) {
		false
	}
	*/
	return hasMethod2(name, parameterTypes)
}

fun SootClass.hasMethod2(name: String, parameterTypes: List<soot.Type>): Boolean {
	val methodsWithName = this.getMethodsWithName(name)
	val result = methodsWithName.any {
		it.parameterTypes == parameterTypes
	}
	return result
}

fun SootClass.getMethodsWithName(name: String): List<SootMethod> {
	return this.methods.filter { it.name == name }
}

fun SootClass.getAllDirectInterfaces(): List<SootClass> {
	return if (interfaceCount == 0) {
		listOf()
	} else {
		val clazzInterfaces = interfaces.toList()
		clazzInterfaces.flatMap { clazzInterfaces + it.getAllDirectInterfaces() }
	}
}

fun SootClass.getAllDirectAndIndirectInterfaces(): List<SootClass> {
	return if (interfaceCount == 0) {
		listOf()
	} else {
		val clazzInterfaces = interfaces.toList()
		if (hasSuperclass()) {
			superclass.getAllDirectAndIndirectInterfaces() + clazzInterfaces.flatMap { clazzInterfaces + it.getAllDirectAndIndirectInterfaces() }
		} else {
			clazzInterfaces.flatMap { clazzInterfaces + it.getAllDirectAndIndirectInterfaces() }
		}
	}
}

val SootMethod.hasBody: Boolean get() = !this.isAbstract && !this.isNative
val SootMethod.isMethodOverriding: Boolean get() = this.overridingMethod != null

fun SootClass.getMethod2(name: String, parameterTypes: List<soot.Type>): SootMethod {
	return this.methods.firstOrNull {
		(it.name == name) && (it.parameterTypes == parameterTypes)
	} ?: throw RuntimeException("Class ${this.name} doesn\'t have method $name($parameterTypes)")
}

val SootMethod.overridingMethod: AstMethodRef? get() {
	val method = this
	val clazz = method.declaringClass
	val name = method.name
	val returnType = method.returnType
	val parameterTypes = method.parameterTypes as List<soot.Type>
	val ancestors = clazz.getAncestors()
	val interfaces = clazz.getAllDirectAndIndirectInterfaces()
	val overrideClass = ancestors.firstOrNull { it.hasMethod(name, parameterTypes) }
	val implementClass = interfaces.firstOrNull { it.hasMethod(name, parameterTypes) }

	//val baseClass = overrideClass ?: implementClass
	val baseClass = overrideClass

	return if (baseClass != null) {
		val overrideMethod = try {
			baseClass.getMethod(name, parameterTypes)
		} catch (e: Throwable) {
			try {
				baseClass.getMethod(name, parameterTypes, returnType)
			} catch (e: Throwable) {
				baseClass.getMethod2(name, parameterTypes)
			}
		}

		if (method.returnType.astType != overrideMethod.returnType.astType) {
			null
		} else {
			AstMethodRef(
				FqName(baseClass.name),
				overrideMethod.name,
				AstType.METHOD_TYPE(
					overrideMethod.returnType.astType,
					overrideMethod.parameterTypes.map { (it as Type).astType }
				)
			)
		}
	} else {
		null
	}
}

val SootMethod.isMethodImplementing: Boolean get() {
	fun locateMethodInInterfaces(clazz: SootClass, method: SootMethod): Boolean {
		val name = method.name
		val parameterTypes = method.parameterTypes

		if (clazz.interfaces.any { locateMethodInInterfaces(it, method) }) return true

		for (interfaze in clazz.interfaces) {
			try {
				interfaze.getMethod(name, parameterTypes)
				return true
			} catch (e: Throwable) {

			}
		}
		return false
	}
	return locateMethodInInterfaces(this.declaringClass, this)
}

fun SootClass.getAllImplementedMethods(): List<AstMethodRef> {
	return this.getAncestors(includeThis = true).flatMap { it.methods }.map { it.astRef }
}

fun SootClass.getAllMethodsToImplement(): List<AstMethodRef> {
	return this.getAllDirectAndIndirectInterfaces().flatMap { it.methods }.map { it.astRef }
}

val SootClass.isClassAbstract: Boolean get() = this.isAbstract && !this.isInterface
