package com.jtransc.ast.dependency

import com.jtransc.ast.*
import com.jtransc.ast.treeshaking.GetTemplateReferencesRefs
import com.jtransc.ast.treeshaking.TRefConfig
import com.jtransc.ast.treeshaking.TRefReason
import com.jtransc.gen.TargetName
import com.jtransc.plugin.JTranscPluginGroup
import com.jtransc.text.INDENTS

//const val SI_TRACE = true
const val SI_TRACE = false

// @TODO: We should evaluate actual AST in order to do this properly (to handle branches properly)
// @TODO: Before modifying this file, please provide tests to avoid future regressions and better
// @TODO: work on this implementation
fun genStaticInitOrder(program: AstProgram, plugins: JTranscPluginGroup) {
	//val classes = LinkedHashSet<AstType.REF>()
	//val processed = LinkedHashSet<AstRef>()
	val targetName = program.injector.get<TargetName>()
	val mainClass = program[program.entrypoint]


	val CLASS = program["java.lang.Class".fqname]

	val inits = program.staticInitsSorted

	val obj = object {
		val visited = hashSetOf<Any>()
		val capturedClass = LinkedHashMap<AstClass, Int>()

		fun getClassLockCount(clazz: AstClass): Int {
			return capturedClass.getOrPut(clazz) { 0 };
		}

		fun lockClass(clazz: AstClass) {
			capturedClass[clazz] = getClassLockCount(clazz) + 1
		}

		fun unlockClass(clazz: AstClass, depth: Int) {
			capturedClass[clazz] = getClassLockCount(clazz) - 1
			if (capturedClass[clazz]!! <= 0) {
				if (SI_TRACE) println("${INDENTS[depth]}Added: ${clazz.ref}")
				inits += clazz.ref

			}
		}

		fun visitMethod(method: AstMethod?, depth: Int) {
			if (method == null) return
			if (!visited.add(method)) return
			val clazz = method.containingClass

			// Totally ignore this class since it references all classes!
			// @TODO: Think about this some more
			if (clazz.fqname == "j.ProgramReflection") return
			if (clazz.fqname == "java.util.ServiceLoader") return

			lockClass(clazz)
			if (SI_TRACE) println("${INDENTS[depth]}Appeared method $method [")


			val refs = if (method.hasDependenciesInBody(targetName)) {
				//method.bodyDependencies.allSortedRefsStaticInit
				AstDependencyAnalyzer.analyze(program, method.body, method.name, config = AstDependencyAnalyzer.Config(
					AstDependencyAnalyzer.Reason.STATIC,
					methodHandler = { expr, da ->
						plugins.onStaticInitHandleMethodCall(program, expr, method.body, da)
					}
				)).allSortedRefsStaticInit
			} else {
				GetTemplateReferencesRefs(program, method.annotationsList.getBodiesForTarget(targetName).map { it.value }.joinToString { "\n" }, clazz.name, config = TRefConfig(reason = TRefReason.STATIC))
			}

			for (ref in refs) {
				if (ref is AstMemberRef) {
					lockClass(program[ref.containingClass])
				}

				if (ref is AstType.REF) {
					lockClass(program[ref]!!)
				}

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

				if (ref is AstType.REF) {
					unlockClass(program[ref]!!, depth)
				}

				if (ref is AstMemberRef) {
					unlockClass(program[ref.containingClass], depth)
				}

			}


			//if ("Easings" in clazz.fqname) println(INDENTS[depth] + "/${clazz.fqname}")

			unlockClass(clazz, depth)

			if (SI_TRACE) println("${INDENTS[depth]}] Disappeared method $method")
		}

		fun visitClass(clazz: AstClass) {
			if (!visited.add(clazz)) return

			if (SI_TRACE) println("Appeared class $clazz")

			lockClass(clazz)

			// Parent classes are loaded before loading this class
			if (clazz.extending != null) visitClass(program[clazz.extending])

			// Later, the static constructor is executed (let's find references without taking branches into account)
			visitMethod(clazz.staticConstructor, 0)

			unlockClass(clazz, 0)
		}
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~ Should be added ProgramReflection first because getClass created static constructor and request Class
	// @NOTE: We can't visit ProgramReflection since we don't know the order of execution
	//obj.visitClass(program[ProgramReflection::class.java.fqname])

	// Also should be added different charsets. We don't see them directly because they added as services
	//for (clazz in program.classes)
	//	for (ancestor in clazz.ancestors)
	//		if(ancestor.name == JTranscCharset::class.java.fqname)
	//			obj.visitClass(program[clazz.name])

	obj.visitClass(mainClass)

	for (clazz in program.classes) obj.visitClass(clazz)

	// Classes without extra static requirements
	for (clazz in program.classes) inits += clazz.ref
}