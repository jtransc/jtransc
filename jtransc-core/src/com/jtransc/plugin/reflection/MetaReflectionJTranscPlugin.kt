package com.jtransc.plugin.reflection

import com.jtransc.annotation.JTranscInvisibleExternal
import com.jtransc.annotation.haxe.HaxeRemoveField
import com.jtransc.ast.*
import com.jtransc.plugin.JTranscPlugin
import com.jtransc.reflection.JTranscInternalNames
import j.ClassInfo
import j.MemberInfo
import j.ProgramReflection

/**
 * This class aims to create classes to perform reflection on available classes
 */
class MetaReflectionJTranscPlugin : JTranscPlugin() {
	override val priority: Int = Int.MAX_VALUE - 1000
	companion object {
		const val CASES_PER_SWITCH: Int = 100
	}

	fun AstClass.mustReflect(invisibleExternalSet: Set<String> = setOf()): Boolean {
		return this.visible && (this.fqname !in invisibleExternalSet) && (this.annotationsList.getNativeNameForTarget(targetName) == null)
	}

	fun AstMethod.mustReflect(): Boolean = this.visible

	fun AstField.mustReflect(): Boolean = this.visible && !this.annotationsList.contains<HaxeRemoveField>()

	override fun processAfterTreeShaking(program: AstProgram) {
		// Do not generate if ProgramReflection class is not referenced!
		// Also methods are not updated in the case they do not exist!
		if (ProgramReflection::class.java.fqname !in program) return
		if (ClassInfo::class.java.fqname !in program) return

		val types = program.types
		val ProgramReflectionClass = program[ProgramReflection::class.java.fqname]

		val annotationFqname = "java.lang.annotation.Annotation".fqname
		val annotationType = AstType.REF(annotationFqname)
		val classesForAnnotations = hashMapOf<AstType.REF, AstClass>()

		fun getAnnotationProxyClass(annotationType: AstType.REF): AstClass {
			return classesForAnnotations.getOrPut(annotationType) {
				val annotationClass = program[annotationType]!!
				val annotationMethods = annotationClass.methods.filter { it.methodType.argCount == 0 }

				val members = annotationMethods.map { it.name to it.methodType.ret }
				val proxyClassName = "${annotationType.fqname}\$Impl"

				val clazz = program.createDataClass(proxyClassName.fqname, members, interfaces = listOf(annotationType.name, annotationFqname)) {
					extraVisible = true

					val annotationTypeMR = AstMethodWithoutClassRef("annotationType", AstType.METHOD(AstType.CLASS, listOf()))
					val getClassMR = AstMethodWithoutClassRef("getClass", AstType.METHOD(AstType.CLASS, listOf()))
					val toStringMR = AstMethodWithoutClassRef("toString", AstType.METHOD(AstType.STRING, listOf()))

					for (m in annotationMethods) {
						val ref = AstMethodWithoutClassRef(m.name, m.methodType)
						if (ref == annotationTypeMR) continue
						if (ref == getClassMR) continue
						if (ref == toStringMR) continue
						createMethod(m.name, m.methodType, asyncOpt = false) {
							extraVisible = false
							RETURN(THIS[locateField(m.name)!!])
						}
					}
					createMethod(annotationTypeMR.name, annotationTypeMR.type, asyncOpt = false) {
						RETURN(annotationType.lit)
					}
					createMethod(getClassMR.name, getClassMR.type, asyncOpt = false) {
						RETURN(annotationType.lit)
					}
					createMethod(toStringMR.name, toStringMR.type, asyncOpt = null) {
						val appendObject = AstMethodRef(StringBuilder::class.java.fqname, "append", AstType.METHOD(AstType.STRINGBUILDER, listOf(AstType.OBJECT)))
						val toString = AstMethodRef(java.lang.Object::class.java.fqname, "toString", AstType.METHOD(AstType.STRING, listOf()))

						var mod: AstExpr = AstExpr.NEW_WITH_CONSTRUCTOR(AstMethodRef(AstType.STRINGBUILDER.name, "<init>", AstType.METHOD(AstType.VOID, listOf())), listOf())

						var buffer = ""
						var pos = 0
						buffer += "@${annotationType.fqname}("
						for (method in annotationMethods) {
							if (pos != 0) buffer += ","
							pos++
							buffer += "${method.name}="
							if (buffer.isNotEmpty()) {
								mod = mod[appendObject](buffer.lit)
								buffer = ""
							}
							mod = mod[appendObject](THIS[method]().castTo(OBJECT))
						}
						mod = mod[appendObject](")".lit)

						RETURN(mod[toString]())
						//RETURN(AstExpr.LITERAL("@${annotationType.fqname}", types))
					}
				}

				//println("Created class: $proxyClassName : ${clazz.visible}")

				clazz
			}
		}

		//println("----------")

		for (clazz in program.classes.toList().filter { it.extendsOrImplements(annotationFqname) && !it.fqname.endsWith("\$Proxy") }) {
			//println("$clazz: ${clazz.extendsOrImplements(annotationFqname)}")

			getAnnotationProxyClass(clazz.ref)
		}

		val invisibleExternalSet = program.allAnnotationsList.getAllTyped<JTranscInvisibleExternal>().flatMap { it.classes.toList() }.toSet()

		val visibleClasses = program.classes.filter { it.mustReflect(invisibleExternalSet) }

		val CLASS_INFO = program[ClassInfo::class.java.fqname]
		val CLASS_INFO_CREATE = CLASS_INFO.getMethodWithoutOverrides(ClassInfo::create.name)!!.ref

		val genInternalClassNames = program.containsMethod(JTranscInternalNames::class.java.fqname, JTranscInternalNames::getInternalClassName.name)
		val genInternalMemberNames = program.containsMethod(JTranscInternalNames::class.java.fqname, JTranscInternalNames::getInternalFieldName.name) || program.containsMethod(JTranscInternalNames::class.java.fqname, JTranscInternalNames::getInternalMethodName.name)

		fun toAnnotationExpr(data: Any?, temps: TempAstLocalFactory, builder: AstBuilder2): AstExpr {
			return builder.run {
				when (data) {
					null -> null.lit
					is AstAnnotation -> {
						if (data.runtimeVisible) {
							val annotationType = program[data.type]
							val annotationProxy = getAnnotationProxyClass(data.type)
							val annotationProxyConstructor = annotationProxy.constructors.first()
							val args = arrayListOf<Any?>()
							for (m in annotationType!!.methods) {
								val value = data.elements[m.name] ?: m.defaultTag
								args += value
							}
							AstExpr.NEW_WITH_CONSTRUCTOR(annotationProxyConstructor.ref, args.map { toAnnotationExpr(it, temps, builder) })
						} else {
							null.lit
						}
					}
					is AstFieldWithoutTypeRef -> toAnnotationExpr(program[data.containingClass].locateField(data.name)!!.ref, temps, builder)
					is AstFieldRef -> AstExpr.FIELD_STATIC_ACCESS(data)
					is Pair<*, *> -> toAnnotationExpr(data.second, temps, builder)
					is List<*> -> {
						val local = temps.create(ARRAY(OBJECT))
						SET(local, (local.type.asArray()).newArray(data.size.lit))
						for ((index, item) in data.withIndex()) {
							SET_ARRAY(local, index.lit, toAnnotationExpr(item, temps, builder))
						}
						local.expr
					}
					is com.jtransc.org.objectweb.asm.Type -> {
						AstType.REF(data.className.fqname).lit
					}
					else -> data.lit
				}
			}
		}

		val ANNOTATION_ARRAY = ARRAY(AstType.REF("java.lang.annotation.Annotation"))

		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getClassAnnotations.name)?.replaceBodyOptBuild { args ->
			val (classIdArg) = args

			val temps = TempAstLocalFactory()

			val cleanVisibleClasses = visibleClasses.filter { it.runtimeAnnotations.isNotEmpty() && "java.lang.annotation.Annotation".fqname !in it.implementing }

			var switchIndex: Int = 0
			while (switchIndex * CASES_PER_SWITCH < cleanVisibleClasses.size) {
				SWITCH(classIdArg.expr) {
					var caseIndex: Int = 0
					while (caseIndex < CASES_PER_SWITCH && (switchIndex * CASES_PER_SWITCH + caseIndex) < cleanVisibleClasses.size) {
						val clazz = cleanVisibleClasses[switchIndex * CASES_PER_SWITCH + caseIndex]
						CASE(clazz.classId) {
							RETURN(ANNOTATION_ARRAY.newLiteralArray(
								clazz.runtimeAnnotations.map { toAnnotationExpr(it, temps, this) }
							))
						}
						caseIndex++
					}
				}
				switchIndex++
			}
			RETURN(NULL)
		}

		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getFieldAnnotations.name)?.replaceBodyOptBuild { args ->
			val (classIdArg, fieldIdArg) = args

			val temps = TempAstLocalFactory()

			val cleanVisibleClasses = visibleClasses.filter { it.fields.filter { it.mustReflect() }.flatMap { it.runtimeAnnotations }.isNotEmpty() }

			var switchIndex: Int = 0
			while (switchIndex * CASES_PER_SWITCH < cleanVisibleClasses.size) {
				SWITCH(classIdArg.expr) {
					var caseIndex: Int = 0
					while (caseIndex < CASES_PER_SWITCH && (switchIndex  * CASES_PER_SWITCH + caseIndex) < cleanVisibleClasses.size) {
						val clazz = cleanVisibleClasses[switchIndex * CASES_PER_SWITCH + caseIndex]
						val fields = clazz.fields.filter { it.mustReflect() }
						CASE(clazz.classId) {
							val classId = clazz.classId
							val perClassMethod = ProgramReflectionClass.createMethod("getFieldAnnotations$classId", AstType.METHOD(AstType.ARRAY(annotationType), AstType.INT), isStatic = true, asyncOpt = false) { args ->
							//val perClassMethod = ProgramReflectionClass.createMethod("getFieldAnnotations$classId", AstType.METHOD(AstType.ARRAY(annotationType), AstType.INT), isStatic = true, asyncOpt = true) { args ->
								val (fieldIdArgIn) = args
								SWITCH(fieldIdArgIn.expr) {
									for (field in fields) {
										val annotations = field.runtimeAnnotations
										if (annotations.isNotEmpty()) {
											CASE(field.id) {
												RETURN(ANNOTATION_ARRAY.newLiteralArray(
													annotations.map { toAnnotationExpr(it, temps, this) }
												))
											}
										}
									}
								}
								RETURN(NULL)
							}

							RETURN(perClassMethod.invoke(fieldIdArg.expr))
						}
						caseIndex++
					}
				}
				switchIndex++
			}
			RETURN(NULL)
		}

		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getMethodAnnotations.name)?.replaceBodyOptBuild { args ->
			val (classIdArg, methodIdArg) = args

			val temps = TempAstLocalFactory()

			SWITCH(classIdArg.expr) {
				for (clazz in visibleClasses) {
					val methods = clazz.methods
					if (methods.flatMap { it.runtimeAnnotations }.isNotEmpty()) {
						CASE(clazz.classId) {
							SWITCH(methodIdArg.expr) {
								for (method in methods) {
									val annotations = method.runtimeAnnotations
									if (annotations.isNotEmpty()) {
										CASE(method.id) {
											RETURN(ANNOTATION_ARRAY.newLiteralArray(
												annotations.map { toAnnotationExpr(it, temps, this) }
											))
										}
									}
								}
							}
						}
					}
				}
			}
			RETURN(NULL)
		}

		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getMethodArgumentAnnotations.name)?.replaceBodyOptBuild { args ->
			val (classIdArg, methodIdArg, argIndexArt) = args

			val temps = TempAstLocalFactory()

			SWITCH(classIdArg.expr) {
				for (clazz in visibleClasses) {
					val methods = clazz.methods.filter { it.mustReflect() }
					if (methods.flatMap { it.parameterAnnotations.flatMap { it } }.isNotEmpty()) {
						CASE(clazz.classId) {
							SWITCH(methodIdArg.expr) {
								for (method in methods) {
									val allParameterAnnotations = method.parameterAnnotations.flatMap { it }
									if (allParameterAnnotations.isNotEmpty()) {
										CASE(method.id) {
											SWITCH(argIndexArt.expr) {
												for (argIndex in 0 until method.methodType.argCount) {
													val annotations = method.parameterAnnotations[argIndex]
													if (annotations.isNotEmpty()) {
														CASE(argIndex) {
															RETURN(ANNOTATION_ARRAY.newLiteralArray(
																annotations.map { toAnnotationExpr(it, temps, this) }
															))
														}
													}
												}
											}
											RETURN(NULL)
										}
									}
								}
							}
						}
					}
				}
			}
			RETURN(NULL)
		}

		val getAllClassesCountMethod: AstMethod? = program[ProgramReflection.AllClasses::class.java.fqname].getMethodWithoutOverrides(ProgramReflection.AllClasses::getAllClassesCount.name)

		getAllClassesCountMethod?.replaceBodyOptBuild {
			RETURN(program.lastClassId.lit)
		}

		val getAllClassesPartMethods: List<AstMethod?> = listOf(
			program[ProgramReflection.AllClasses::class.java.fqname].getMethodWithoutOverrides(ProgramReflection.AllClasses::getAllClasses3000.name),
			program[ProgramReflection.AllClasses::class.java.fqname].getMethodWithoutOverrides(ProgramReflection.AllClasses::getAllClasses6000.name),
			program[ProgramReflection.AllClasses::class.java.fqname].getMethodWithoutOverrides(ProgramReflection.AllClasses::getAllClasses9000.name),
			program[ProgramReflection.AllClasses::class.java.fqname].getMethodWithoutOverrides(ProgramReflection.AllClasses::getAllClasses12000.name),
			program[ProgramReflection.AllClasses::class.java.fqname].getMethodWithoutOverrides(ProgramReflection.AllClasses::getAllClasses15000.name),
			program[ProgramReflection.AllClasses::class.java.fqname].getMethodWithoutOverrides(ProgramReflection.AllClasses::getAllClassesMax.name)
		)

		for ((part, getAllClassesPartMethod) in getAllClassesPartMethods.withIndex()) {
			getAllClassesPartMethod?.replaceBodyOptBuild {
				val out = AstLocal(0, "out", ARRAY(CLASS_INFO))
				var partVisibleClasses: List<AstClass> = visibleClasses.filter { it.classId in part*3000..((part+1)*3000-1) }.sortedBy { it.classId }
				if (part == getAllClassesPartMethods.size - 1) {
					partVisibleClasses =  visibleClasses.filter { it.classId >= 15000 }.sortedBy { it.classId }
				}
				SET(out, ARRAY(CLASS_INFO).newArray(partVisibleClasses.size))
				for ((index, oldClass) in partVisibleClasses.withIndex()) {
					val classId = oldClass.classId
					val directInterfaces = oldClass.directInterfaces
					val relatedTypes = oldClass.getAllRelatedTypesIdsWithout0AtEnd()
					SET_ARRAY(out, index.lit, CLASS_INFO_CREATE(
						classId.lit,
						if (genInternalClassNames) AstExpr.LITERAL_REFNAME(oldClass.ref) else NULL,
						oldClass.fqname.lit,
						oldClass.modifiers.acc.lit,
						(oldClass.parentClass?.classId ?: -1).lit,
						if (directInterfaces.isNotEmpty()) AstExpr.INTARRAY_LITERAL(directInterfaces.map { it.classId }) else null.lit,
						if (relatedTypes.isNotEmpty()) AstExpr.INTARRAY_LITERAL(relatedTypes) else null.lit
					))
				}
				RETURN(out.local)
			}
		}

		// @TODO: We should create a submethod per class to avoid calling ::SI (static initialization) for all the classes
		if (program.contains(ProgramReflection.DynamicInvoke::class.java.fqname)) {
			val dynamicInvokeClass: AstClass = program[ProgramReflection.DynamicInvoke::class.java.fqname]
			val dynamicInvokeMethod: AstMethod? = dynamicInvokeClass.getMethodWithoutOverrides(ProgramReflection.DynamicInvoke::dynamicInvoke.name)

			if (dynamicInvokeMethod != null) {
				val additionalMethods: MutableList<AstMethod> = mutableListOf()
				val classes: List<AstMethod> = visibleClasses.flatMap { it.methodsWithoutConstructors.filter { it.mustReflect() } }.sortedBy { it.id }

				val dynamicInvokeClasses :List<AstClass?> = listOf(
					program[ProgramReflection.DynamicInvokeFirst::class.java.fqname],
					program[ProgramReflection.DynamicInvokeMiddle::class.java.fqname],
					program[ProgramReflection.DynamicInvokeLast::class.java.fqname]
				)

				var methodIndex: Int = 0
				while (methodIndex * CASES_PER_SWITCH < classes.size) {
					val mI: Int = methodIndex
					val sI: Int = methodIndex * CASES_PER_SWITCH

					val currentDynamicInvokeClass = dynamicInvokeClasses[methodIndex % dynamicInvokeClasses.size]
					val currentDynamicInvokeMethod: AstMethod? = currentDynamicInvokeClass!!.getMethodWithoutOverrides(ProgramReflection.DynamicInvoke::dynamicInvoke.name)
					val newMethod: AstMethod =
						currentDynamicInvokeClass.createMethod(currentDynamicInvokeMethod!!.name + mI, currentDynamicInvokeMethod.methodType, true, asyncOpt = true) {
							val (classId, methodId, obj, args) = it
							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + CASES_PER_SWITCH < classes.size) currentIndex + CASES_PER_SWITCH else classes.size

							SWITCH(methodId.expr) {
								while (currentIndex < finishIndex) {
									val method = classes[currentIndex]

									val params: List<AstExpr> = method.methodType.args.map {
										AstExpr.ARRAY_ACCESS(args.expr, it.index.lit).castTo(it.type)
									}

									val callExprUncasted = if (method.isStatic) {
										AstExpr.CALL_STATIC(method.containingClass.ref, method.ref, params)
									} else {
										AstExpr.CALL_INSTANCE(obj.expr.castTo(method.containingClass.ref), method.ref, params)
									}

									CASE(method.id) {
										if (method.methodType.retVoid) {
											STM(callExprUncasted)
											RETURN(NULL)
										} else {
											RETURN(callExprUncasted.castTo(OBJECT))
										}
									}
									currentIndex++
								}
							}
							RETURN(NULL)
						}
					additionalMethods.add(newMethod)
					methodIndex++
				}

				dynamicInvokeMethod.replaceBodyOptBuild {
					val (classId, methodId, obj, args) = it
					var id: Int = additionalMethods.size

					while (--id >= 0) {
						val method: AstMethod = additionalMethods[id]
						val constructor: AstMethod = classes[id * CASES_PER_SWITCH]
						IF(AstExpr.BINOP(AstType.BOOL, methodId.expr, AstBinop.GE, constructor.id.lit)) {
							val params: List<AstExpr> = listOf(classId.expr, methodId.expr, obj.expr, args.expr)
							val callExprUncasted = AstExpr.CALL_STATIC(method.ref, params)
							RETURN(callExprUncasted.castTo(OBJECT))
						}
					}
					RETURN(NULL)
				}
			}
		}


		// @TODO: We should create a submethod per class to avoid calling ::SI (static initialization) for all the classes
		if (program.contains(ProgramReflection.DynamicNew::class.java.fqname)) {
			val dynamicNewClass: AstClass = program[ProgramReflection.DynamicNew::class.java.fqname]
			val dynamicNewMethod: AstMethod? = dynamicNewClass.getMethodWithoutOverrides(ProgramReflection.DynamicNew::dynamicNew.name)

			if (dynamicNewMethod != null) {
				val additionalMethods: MutableList<AstMethod> = mutableListOf()
				val classes: List<AstMethod> = visibleClasses.filter { !it.isAbstract && !it.isInterface }.flatMap { it.constructors.filter { it.mustReflect() } }.sortedBy { it.id }

				var methodIndex: Int = 0
				while (methodIndex * CASES_PER_SWITCH < classes.size) {
					val mI: Int = methodIndex
					val sI: Int = methodIndex * CASES_PER_SWITCH

					val newMethod: AstMethod =
						dynamicNewClass.createMethod(dynamicNewMethod.name + mI, dynamicNewMethod.methodType, true, asyncOpt = true) {
							val (classId, methodId, args) = it
							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + CASES_PER_SWITCH < classes.size) currentIndex + CASES_PER_SWITCH else classes.size

							SWITCH(methodId.expr) {
								while (currentIndex < finishIndex) {
									val constructor = classes[currentIndex]

									CASE(constructor.id) {
										val params = constructor.methodType.args.map {
											AstExpr.ARRAY_ACCESS(args.expr, it.index.lit).castTo(it.type)
										}

										val callExprUncasted = AstExpr.NEW_WITH_CONSTRUCTOR(constructor.ref, params)

										RETURN(callExprUncasted.castTo(OBJECT))
									}

									currentIndex++
								}
							}
							RETURN(NULL)
						}
					additionalMethods.add(newMethod)
					methodIndex++
				}

				dynamicNewMethod.replaceBodyOptBuild {
					val (classId, methodId, args) = it
					var id: Int = additionalMethods.size

					while (--id >= 0) {
						val method: AstMethod = additionalMethods[id]
						val constructor: AstMethod = classes[id * CASES_PER_SWITCH]
						IF(AstExpr.BINOP(AstType.BOOL, methodId.expr, AstBinop.GE, constructor.id.lit)) {
							val params: List<AstExpr> = listOf(classId.expr, methodId.expr, args.expr)
							val callExprUncasted = AstExpr.CALL_STATIC(method.ref, params)
							RETURN(callExprUncasted.castTo(OBJECT))
						}
					}

					RETURN(NULL)
				}
			}
		}


		// Member information (constructors, methods and fields)
		if (MemberInfo::class.java.fqname in program) {
			val MemberInfoClass = program[MemberInfo::class.java.fqname]
			val MemberInfo_create = MemberInfoClass.getMethodWithoutOverrides(MemberInfo::create.name)!!.ref

			data class MemberInfoWithRef(val ref: Any, val mi: MemberInfo)

			fun AstBuilder2.genMemberList(args: List<AstArgument>, list: List<Pair<AstClass, List<MemberInfoWithRef>>>, additionalMethods: List<AstMethod>) {
				val (classIdArg) = args
				var id: Int = additionalMethods.size

				while (--id >= 0) {
					val method: AstMethod = additionalMethods[id]
					val (keyClass, members) = list[id * CASES_PER_SWITCH]
					IF(AstExpr.BINOP(AstType.BOOL, classIdArg.expr, AstBinop.GE, keyClass.classId.lit)) {
						val params = listOf(classIdArg.expr)
						val callExprUncasted = AstExpr.CALL_STATIC(method.ref, params)

						RETURN(callExprUncasted.castTo(ARRAY(MemberInfoClass)))
					}
				}

				RETURN(NULL)
			}

			fun genMemberMethods(list: List<Pair<AstClass, List<MemberInfoWithRef>>>, classes: List<AstClass>, methodName: String, generalClass: AstClass) {

				val additionalMethods: MutableList<AstMethod> = mutableListOf()
				var methodIndex: Int = 0
				while (methodIndex * CASES_PER_SWITCH < list.size) {
					val mI: Int = methodIndex
					val sI: Int = methodIndex * CASES_PER_SWITCH
					val parentClass: AstClass = classes[methodIndex % classes.size]
					val parentMethod: AstMethod? = parentClass.getMethodWithoutOverrides(methodName)

					val newMethod: AstMethod =
						parentClass.createMethod(parentMethod!!.name + mI, parentMethod.methodType, true, asyncOpt = false) {
							val (classIdArg) = it
							val out = AstLocal(0, "out", ARRAY(MemberInfoClass))

							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + CASES_PER_SWITCH < list.size) currentIndex + CASES_PER_SWITCH else list.size

							SWITCH(classIdArg.expr) {
								while (currentIndex < finishIndex) {
									val (keyClass, members) = list[currentIndex]
									if (members.isNotEmpty()) {
										CASE(keyClass.classId) {
											SET(out, ARRAY(MemberInfoClass).newArray(members.size.lit))

											for ((index, memberWithRef) in members.withIndex()) {
												val ref = memberWithRef.ref
												val member = memberWithRef.mi

												SET_ARRAY(out, index.lit, MemberInfo_create(
													member.id.lit,
													if (genInternalMemberNames) AstExpr.LITERAL_REFNAME(ref) else NULL,
													member.name.lit,
													member.modifiers.lit,
													member.desc.lit,
													member.genericDesc.lit
												))
											}
											RETURN(out.local)
										}
									}
									currentIndex++
								}
							}
							RETURN(NULL)
						}
					additionalMethods.add(newMethod)
					methodIndex++
				}

				generalClass.getMethodWithoutOverrides(methodName)!!.replaceBodyOptBuild {
					genMemberList(it, list, additionalMethods)
				}
			}

			// ProgramReflectionClass.getConstructors
			if (program.contains(ProgramReflection.AllConstructors::class.java.fqname)) {
				val list: List<Pair<AstClass, List<MemberInfoWithRef>>> = visibleClasses.map {
					it to it.constructors.filter { it.mustReflect() }.map {
						MemberInfoWithRef(it.ref, MemberInfo(it.id, null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
					}
				}.toList()
				val allConstructorsClasses :List<AstClass> = listOf(
					program[ProgramReflection.AllConstructorsFirst::class.java.fqname],
					program[ProgramReflection.AllConstructorsMiddle::class.java.fqname],
					program[ProgramReflection.AllConstructorsLast::class.java.fqname]
				)
				genMemberMethods(list, allConstructorsClasses, ProgramReflection.AllConstructors::getConstructors.name, program[ProgramReflection.AllConstructors::class.java.fqname])
			}

			// ProgramReflectionClass.getMethods
			if (program.contains(ProgramReflection.AllMethods::class.java.fqname)) {
				val list: List<Pair<AstClass, List<MemberInfoWithRef>>> = visibleClasses.map {
					it to it.methodsWithoutConstructors.filter { it.mustReflect() }.map {
						MemberInfoWithRef(it.ref, MemberInfo(it.id, null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
					}
				}.toList()
				val allMethodsClasses :List<AstClass> = listOf(
					program[ProgramReflection.AllMethodsFirst::class.java.fqname],
					program[ProgramReflection.AllMethodsMiddle::class.java.fqname],
					program[ProgramReflection.AllMethodsLast::class.java.fqname]
				)
				genMemberMethods(list, allMethodsClasses, ProgramReflection.AllMethods::getMethods.name, program[ProgramReflection.AllMethods::class.java.fqname])
			}

			// ProgramReflectionClass.getFields
			if (program.contains(ProgramReflection.AllFields::class.java.fqname)) {
				val list: List<Pair<AstClass, List<MemberInfoWithRef>>> = visibleClasses.map {
					it to it.fields.filter { it.mustReflect() }.map {
						MemberInfoWithRef(it.ref, MemberInfo(it.id, null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
					}
				}.toList()
				val allFieldsClasses :List<AstClass> = listOf(
					program[ProgramReflection.AllFieldsFirst::class.java.fqname],
					program[ProgramReflection.AllFieldsMiddle::class.java.fqname],
					program[ProgramReflection.AllFieldsLast::class.java.fqname]
				)
				genMemberMethods(list, allFieldsClasses, ProgramReflection.AllFields::getFields.name, program[ProgramReflection.AllFields::class.java.fqname])
			}

			// ProgramReflectionClass.dynamicGet
			if (program.contains(ProgramReflection.DynamicGet::class.java.fqname)) {
				val dynamicGetClass: AstClass = program[ProgramReflection.DynamicGet::class.java.fqname]
				val dynamicGetMethod: AstMethod? = dynamicGetClass.getMethodWithoutOverrides(ProgramReflection.DynamicGet::dynamicGet.name)

				if (dynamicGetMethod != null) {
					val additionalMethods: MutableList<AstMethod> = mutableListOf()
					val fields: List<AstField> = visibleClasses.flatMap { it.fields.filter { it.mustReflect() } }.sortedBy { it.id }

					var methodIndex: Int = 0
					while (methodIndex * CASES_PER_SWITCH < fields.size) {
						val mI: Int = methodIndex
						val sI: Int = methodIndex * CASES_PER_SWITCH

						val newMethod: AstMethod = dynamicGetClass.createMethod(dynamicGetMethod.name + mI, dynamicGetMethod.methodType, true, asyncOpt = false) {
							val (classId, fieldId, objParam) = it
							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + CASES_PER_SWITCH < fields.size) currentIndex + CASES_PER_SWITCH else fields.size

							SWITCH(fieldId.expr) {
								while (currentIndex < finishIndex) {
									val field = fields[currentIndex]

									val expr = if (field.isStatic) {
										AstExpr.FIELD_STATIC_ACCESS(field.ref)
									} else {
										AstExpr.FIELD_INSTANCE_ACCESS(field.ref, objParam.expr.castTo(field.containingClass.astType))
									}

									CASE(field.id) {
										RETURN(expr.castTo(OBJECT))
									}
									currentIndex++
								}
							}
							RETURN(NULL)
						}
						additionalMethods.add(newMethod)
						methodIndex++
					}

					dynamicGetMethod.replaceBodyOptBuild {
						val (classId, fieldId, objParam) = it
						var id: Int = additionalMethods.size

						while (--id >= 0) {
							val method: AstMethod = additionalMethods[id]
							val field: AstField = fields[id * CASES_PER_SWITCH]
							IF(AstExpr.BINOP(AstType.BOOL, fieldId.expr, AstBinop.GE, field.id.lit)) {
								val params = listOf(classId.expr, fieldId.expr, objParam.expr)
								val callExprUncasted = AstExpr.CALL_STATIC(method.ref, params)
								RETURN(callExprUncasted.castTo(OBJECT))
							}
						}
						RETURN(NULL)
					}
				}
			}


			// ProgramReflectionClass.dynamicSet
			if (program.contains(ProgramReflection.DynamicSet::class.java.fqname)) {
				val dynamicSetClass: AstClass = program[ProgramReflection.DynamicSet::class.java.fqname]
				val dynamicSetMethod: AstMethod? = dynamicSetClass.getMethodWithoutOverrides(ProgramReflection.DynamicSet::dynamicSet.name)

				if (dynamicSetMethod != null) {
					val additionalMethods: MutableList<AstMethod> = mutableListOf()
					val fields: List<AstField> = visibleClasses.flatMap { it.fields.filter { it.mustReflect() } }.sortedBy { it.id }

					var methodIndex: Int = 0
					while (methodIndex * CASES_PER_SWITCH < fields.size) {
						val mI: Int = methodIndex
						val sI: Int = methodIndex * CASES_PER_SWITCH

						val newMethod: AstMethod = dynamicSetClass.createMethod(dynamicSetMethod.name + mI, dynamicSetMethod.methodType, true, asyncOpt = false) {
							val (classIdParam, fieldIdParam, objParam, valueParam) = it
							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + CASES_PER_SWITCH < fields.size) currentIndex + CASES_PER_SWITCH else fields.size

							SWITCH(fieldIdParam.expr) {
								while (currentIndex < finishIndex) {
									val field = fields[currentIndex]
									val expr = valueParam.expr.castTo(field.type)
									CASE(field.id) {
										if (field.isStatic) {
											STM(AstStm.SET_FIELD_STATIC(field.ref, expr))
										} else {
											STM(AstStm.SET_FIELD_INSTANCE(field.ref, objParam.expr.castTo(field.containingClass.astType), expr))
										}
										AstStm.BREAK()
									}
									currentIndex++
								}
							}
						}
						additionalMethods.add(newMethod)
						methodIndex++
					}

					dynamicSetMethod.replaceBodyOptBuild {
						val (classIdParam, fieldIdParam, objParam, valueParam) = it
						var id: Int = additionalMethods.size

						while (--id >= 0) {
							val method: AstMethod = additionalMethods[id]
							val field: AstField = fields[id * CASES_PER_SWITCH]
							IF(AstExpr.BINOP(AstType.BOOL, fieldIdParam.expr, AstBinop.GE, field.id.lit)) {
								val params = listOf(classIdParam.expr, fieldIdParam.expr, objParam.expr, valueParam.expr)
								STM(AstExpr.CALL_STATIC(method.ref, params))
								RETURN()
							}
						}
					}
				}
			}
		}
	}
}

