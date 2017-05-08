package com.jtransc.ast.dependency

import com.jtransc.ast.*
import com.jtransc.ast.treeshaking.GetTemplateReferencesRefs
import com.jtransc.gen.TargetName
import java.util.*
import com.jtransc.charset.*
import j.ProgramReflection

fun genStaticInitOrder(program: AstProgram) {
	//val classes = LinkedHashSet<AstType.REF>()
	//val processed = LinkedHashSet<AstRef>()
	val targetName = program.injector.get<TargetName>()
	val mainClass = program[program.entrypoint]

	val CLASS = program["java.lang.Class".fqname]

	val inits = program.staticInitsSorted

	val obj = object {
		val visited = hashSetOf<Any>()
		val capturedClass = LinkedHashSet<AstClass>()

		fun visitMethod(method: AstMethod?, depth: Int) {
			if (method == null) return
			if (!visited.add(method)) return
			val clazz = method.containingClass
			val captured = capturedClass.add(clazz)
			//println("Appeared $method")

			val refs = if (method.hasDependenciesInBody(targetName)) {
				method.bodyDependencies.allSortedRefsStaticInit
			} else {
				GetTemplateReferencesRefs(program, method.annotationsList.getBodiesForTarget(targetName).map { it.value }.joinToString { "\n" }, clazz.name)
			}

			for (ref in refs) {
				// First visit static constructor
				if (ref is AstMethodRef) {
					visitMethod(program[ref.containingClass].staticConstructor, depth + 1)
				}

				// Then visit methods
				when (ref) {
					is AstType.REF -> {
						val scons = program[ref]?.staticConstructor
						//println(">>>>>>>>>>>>>>> : $ref ($scons)")
						visitMethod(CLASS.staticConstructor, depth + 1)
						//println("Added: ${method.containingClass.ref}")
						//inits += clazz.ref
						if (scons != null) visitMethod(scons, depth + 1)
						//println("<<<<<<<<<<<<<<<")
					}
					is AstMethodRef -> {
						visitMethod(program[ref], depth + 1)
					}
				}
			}


			//if ("Easings" in clazz.fqname) println(INDENTS[depth] + "/${clazz.fqname}")

			if (captured) {
				//println("Added: ${method.containingClass.ref}")
				inits += method.containingClass.ref
			}
		}

		fun visitClass(clazz: AstClass) {
			if (!visited.add(clazz)) return

			// Parent classes are loaded before loading this class
			if (clazz.extending != null) visitClass(program[clazz.extending])

			// Later, the static constructor is executed (let's find references without taking branches into account)
			visitMethod(clazz.staticConstructor, 0)

			inits += clazz.ref
		}
	}

	// Should be added ProgramReflection first because getClass created static constructor and request Class
	obj.visitClass(program[ProgramReflection::class.java.fqname])
	// Also should be added different charsets. We don't see them directly because they added as services
	for (clazz in program.classes)
		for (ancestor in clazz.ancestors)
			if(ancestor.name == JTranscCharset::class.java.fqname)
				obj.visitClass(program[clazz.name])

	obj.visitClass(mainClass)

	for (clazz in program.classes) obj.visitClass(clazz)

	// Classes without extra static requirements
	for (clazz in program.classes) inits += clazz.ref
}