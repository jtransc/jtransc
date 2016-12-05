package com.jtransc.ast.dependency

import com.jtransc.ast.*
import com.jtransc.gen.TargetName

fun genStaticInitOrder(program: AstProgram) {
	//val classes = LinkedHashSet<AstType.REF>()
	//val processed = LinkedHashSet<AstRef>()
	val targetName = program.injector.get<TargetName>()
	val mainClass = program[program.entrypoint]
	val obj = object {
		val visited = hashSetOf<Any>()
		val inits = program.staticInitsSorted

		fun visitType(type: AstType) {
			for (t in type.getRefTypes()) {
				visitClass(program[t]!!)
			}
		}

		fun visitField(field: AstField) {
			if (!visited.add(field)) return
			visitType(field.type)
		}

		fun visitMethod(method: AstMethod) {
			if (!visited.add(method)) return

			for (argType in method.methodType.argsPlusReturn) {
				visitType(argType)
			}

			if (method.hasDependenciesInBody(targetName)) {
				val refs = method.bodyDependencies.allSortedRefs
				for (ref in refs) {
					when (ref) {
						is AstType.REF -> {
							visitType(ref)
						}
						is AstMethodRef -> {
							visitMethod(program[ref]!!)
						}
						is AstFieldRef -> {
							visitField(program[ref]!!)
						}
					}
				}
			}
		}

		fun visitClass(clazz: AstClass) {
			if (!visited.add(clazz)) return

			for (int in clazz.allDirectInterfaces) {
				visitClass(int)
			}

			if (clazz.extending != null) {
				visitClass(program[clazz.extending])
			}

			if (clazz.staticConstructor != null) {
				visitMethod(clazz.staticConstructor!!)
			}

			//for (v in clazz.constructors) visitMethod(v)
			//for (v in clazz.methodsWithoutConstructors) visitMethod(v)
			//for (v in clazz.fields) visitField(v)

			//if (clazz.hasStaticInit) {
			inits += clazz.ref
			//}
		}

		fun visitClassExtra(clazz: AstClass) {
			//visitClass(clazz)
			for (v in clazz.methods) visitMethod(v)
			for (v in clazz.fields) visitField(v)
		}
	}
	obj.visitClass(mainClass)
	for (clazz in program.classes) obj.visitClassExtra(clazz)
	for (clazz in program.classes) obj.visitClass(clazz)
	//val staticInits = program.staticInitsSorted
	//println(program.staticInitsSorted)
}