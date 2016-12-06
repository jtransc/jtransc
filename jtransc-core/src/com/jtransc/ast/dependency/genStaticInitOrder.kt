package com.jtransc.ast.dependency

import com.jtransc.ast.*
import com.jtransc.gen.TargetName
import com.jtransc.text.INDENTS
import com.jtransc.text.Indenter

fun genStaticInitOrder(program: AstProgram) {
	//val classes = LinkedHashSet<AstType.REF>()
	//val processed = LinkedHashSet<AstRef>()
	val targetName = program.injector.get<TargetName>()
	val mainClass = program[program.entrypoint]

	val CLASS = program["java.lang.Class".fqname]

	val inits = program.staticInitsSorted

	val obj = object {
		val visited = hashSetOf<Any>()
		val capturedClass = hashSetOf<Any>()

		fun visitMethod(method: AstMethod?, depth: Int) {
			if (method == null) return
			if (!visited.add(method)) return
			val clazz = method.containingClass
			val captured = capturedClass.add(clazz)
			// Totally ignore this class since it references all classes!
			// @TODO: Think about this some more
			if (clazz.fqname == "j.ProgramReflection") return

			//if ("Easings" in clazz.fqname) println(INDENTS[depth] + "${clazz.fqname}")

			if (method.hasDependenciesInBody(targetName)) {
				val refs = method.bodyDependencies.allSortedRefs
				for (ref in refs) {
					// First visit static constructor
					if (ref is AstMemberRef) {
						visitMethod(program[ref.containingClass].staticConstructor, depth + 1)
					}

					// Then visit methods
					when (ref) {
						is AstType.REF -> {
							visitMethod(CLASS.staticConstructor, depth + 1)
						}
						is AstMethodRef -> {
							visitMethod(program[ref], depth + 1)
						}
					}
				}
			}

			//if ("Easings" in clazz.fqname) println(INDENTS[depth] + "/${clazz.fqname}")

			if (captured) {
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
	obj.visitClass(mainClass) // Not required, since this should work in any order
	for (clazz in program.classes) obj.visitClass(clazz)

	// Classes without extra static requirements
	for (clazz in program.classes) inits += clazz.ref
}