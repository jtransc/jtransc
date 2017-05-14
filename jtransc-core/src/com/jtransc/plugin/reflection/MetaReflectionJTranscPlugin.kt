package com.jtransc.plugin.reflection

import com.jtransc.annotation.JTranscInvisibleExternal
import com.jtransc.annotation.JTranscNativeClass
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
	override val priority: Int = Int.MAX_VALUE

	fun AstClass.mustReflect(invisibleExternalSet: Set<String> = setOf()): Boolean {
		return this.visible && (this.fqname !in invisibleExternalSet) && !this.annotationsList.contains<JTranscNativeClass>()
	}

	fun AstMethod.mustReflect(): Boolean {
		return this.visible
	}

	fun AstField.mustReflect(): Boolean {
		return this.visible && !this.annotationsList.contains<HaxeRemoveField>()
	}

	override fun processAfterTreeShaking(program: AstProgram) {
		// Do not generate if ProgramReflection class is not referenced!
		// Also methods are not updated in the case they do not exist!
		if (ProgramReflection::class.java.fqname !in program) return
		if (ClassInfo::class.java.fqname !in program) return

		val types = program.types
		val ProgramReflectionClass = program[ProgramReflection::class.java.fqname]

		val annotationFqname = "java.lang.annotation.Annotation".fqname
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
						createMethod(m.name, m.methodType) {
							extraVisible = false
							RETURN(THIS[locateField(m.name)!!])
						}
					}
					createMethod(annotationTypeMR.name, annotationTypeMR.type) {
						RETURN(annotationType.lit)
					}
					createMethod(getClassMR.name, getClassMR.type) {
						RETURN(annotationType.lit)
					}
					createMethod(toStringMR.name, toStringMR.type) {
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
			val outLocal = AstLocal(0, "out", ANNOTATION_ARRAY)

			SWITCH(classIdArg.expr) {
				for (clazz in visibleClasses) {
					val annotations = clazz.runtimeAnnotations
					if (annotations.isNotEmpty() && "java.lang.annotation.Annotation".fqname !in clazz.implementing) {
						CASE(clazz.classId) {
							SET(outLocal, ANNOTATION_ARRAY.newArray(annotations.size.lit))
							for ((index, annotation) in annotations.withIndex()) {
								SET_ARRAY(outLocal, index.lit, toAnnotationExpr(annotation, temps, this))
							}
							RETURN(outLocal)
						}
					}
				}
			}
			RETURN(NULL)
		}

		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getFieldAnnotations.name)?.replaceBodyOptBuild { args ->
			val (classIdArg, fieldIdArg) = args

			val temps = TempAstLocalFactory()
			val outLocal = AstLocal(0, "out", ANNOTATION_ARRAY)

			SWITCH(classIdArg.expr) {
				for (clazz in visibleClasses) {
					val fields = clazz.fields.filter { it.mustReflect() }
					if (fields.flatMap { it.runtimeAnnotations }.isNotEmpty()) {
						CASE(clazz.classId) {
							SWITCH(fieldIdArg.expr) {
								for (field in fields) {
									val annotations = field.runtimeAnnotations
									if (annotations.isNotEmpty()) {
										CASE(field.id) {
											SET(outLocal, ANNOTATION_ARRAY.newArray(annotations.size.lit))
											for ((index, annotation) in annotations.withIndex()) {
												SET_ARRAY(outLocal, index.lit, toAnnotationExpr(annotation, temps, this))
											}
											RETURN(outLocal)
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

		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getMethodAnnotations.name)?.replaceBodyOptBuild { args ->
			val (classIdArg, methodIdArg) = args

			val temps = TempAstLocalFactory()
			val outLocal = AstLocal(0, "out", ANNOTATION_ARRAY)

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
											SET(outLocal, ANNOTATION_ARRAY.newArray(annotations.size.lit))
											for ((index, annotation) in annotations.withIndex()) {
												SET_ARRAY(outLocal, index.lit, toAnnotationExpr(annotation, temps, this))
											}
											RETURN(outLocal)
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
			val outLocal = AstLocal(0, "out", ANNOTATION_ARRAY)

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
															SET(outLocal, ANNOTATION_ARRAY.newArray(annotations.size.lit))
															for ((index, annotation) in annotations.withIndex()) {
																SET_ARRAY(outLocal, index.lit, toAnnotationExpr(annotation, temps, this))
															}
															RETURN(outLocal)
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

		val casesPerMethod: Int = 100

		val getAllClassesMethod: AstMethod? =
			if (program.contains(ProgramReflection.AllClasses::class.java.fqname))
				program[ProgramReflection.AllClasses::class.java.fqname].getMethodWithoutOverrides(ProgramReflection.AllClasses::getAllClasses.name)
			else
				ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getAllClasses.name)

		getAllClassesMethod?.replaceBodyOptBuild {
			val out = AstLocal(0, "out", ARRAY(CLASS_INFO))

			SET(out, ARRAY(CLASS_INFO).newArray(program.lastClassId.lit))

			for (oldClass in visibleClasses.sortedBy { it.classId }) {
				val classId = oldClass.classId
				//println("CLASS: ${oldClass.fqname}")
				SET_ARRAY(out, classId.lit, CLASS_INFO_CREATE(
					classId.lit,
					if (genInternalClassNames) AstExpr.LITERAL_REFNAME(oldClass.ref) else NULL,
					oldClass.fqname.lit,
					oldClass.modifiers.acc.lit,
					(oldClass.parentClass?.classId ?: -1).lit,
					AstExpr.INTARRAY_LITERAL(oldClass.directInterfaces.map { it.classId }),
					AstExpr.INTARRAY_LITERAL(oldClass.getAllRelatedTypesIdsWithout0AtEnd())
				))
			}
			RETURN(out.local)
		}


		// @TODO: We should create a submethod per class to avoid calling ::SI (static initialization) for all the classes
		if (program.contains(ProgramReflection.DynamicNewInvoke::class.java.fqname)) {
			val dynamicNewInvokeClass: AstClass = program[ProgramReflection.DynamicNewInvoke::class.java.fqname]
			val dynamicInvokeMethod: AstMethod? = dynamicNewInvokeClass.getMethodWithoutOverrides(ProgramReflection.DynamicNewInvoke::dynamicInvoke.name)

			if (dynamicInvokeMethod != null) {
				val additionalMethods: MutableList<AstMethod> = mutableListOf()
				val classes: List<AstMethod> = visibleClasses.flatMap { it.methodsWithoutConstructors.filter { it.mustReflect() } }.sortedBy { it.id }

				var methodIndex: Int = 0
				while (methodIndex * casesPerMethod < classes.size) {
					val mI: Int = methodIndex
					val sI: Int = methodIndex * casesPerMethod

					val newMethod: AstMethod =
						dynamicNewInvokeClass.createMethod(dynamicInvokeMethod.name + mI, dynamicInvokeMethod.methodType, true) {
							val (classId, methodId, obj, args) = it
							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + casesPerMethod < classes.size) currentIndex + casesPerMethod else classes.size

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
						val constructor: AstMethod = classes[id * casesPerMethod]
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
		if (program.contains(ProgramReflection.DynamicNewInvoke::class.java.fqname)) {
			val dynamicNewInvokeClass: AstClass = program[ProgramReflection.DynamicNewInvoke::class.java.fqname]
			val dynamicNewMethod: AstMethod? = dynamicNewInvokeClass.getMethodWithoutOverrides(ProgramReflection.DynamicNewInvoke::dynamicNew.name)

			if (dynamicNewMethod != null) {
				val additionalMethods: MutableList<AstMethod> = mutableListOf()
				val classes: List<AstMethod> = visibleClasses.filter { !it.isAbstract && !it.isInterface }.flatMap { it.constructors.filter { it.mustReflect() } }.sortedBy { it.id }

				var methodIndex: Int = 0
				while (methodIndex * casesPerMethod < classes.size) {
					val mI: Int = methodIndex
					val sI: Int = methodIndex * casesPerMethod

					val newMethod: AstMethod =
						dynamicNewInvokeClass.createMethod(dynamicNewMethod.name + mI, dynamicNewMethod.methodType, true) {
							val (classId, methodId, args) = it
							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + casesPerMethod < classes.size) currentIndex + casesPerMethod else classes.size

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
						val constructor: AstMethod = classes[id * casesPerMethod]
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
			//val MemberInfo_createList = MemberInfoClass.getMethodWithoutOverrides(MemberInfo::createList.name)!!.ref

			data class MemberInfoWithRef(val ref: Any, val mi: MemberInfo)

			fun genMemberMethods(list: List<Pair<AstClass, List<MemberInfoWithRef>>>, clazz: AstClass, method: AstMethod): List<AstMethod> {
				val additionalMethods: MutableList<AstMethod> = mutableListOf()
				var methodIndex: Int = 0
				while (methodIndex * casesPerMethod < list.size) {
					val mI: Int = methodIndex
					val sI: Int = methodIndex * casesPerMethod
					val parentClass: AstClass = clazz
					val parentMethod: AstMethod = method

					val newMethod: AstMethod =
						parentClass.createMethod(parentMethod.name + mI, parentMethod.methodType, true) {
							val (classIdArg) = it
							val out = AstLocal(0, "out", ARRAY(MemberInfoClass))

							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + casesPerMethod < list.size) currentIndex + casesPerMethod else list.size

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
				return additionalMethods
			}

			fun AstBuilder2.genMemberList(args: List<AstArgument>, list: List<Pair<AstClass, List<MemberInfoWithRef>>>, additionalMethods: List<AstMethod>) {
				val (classIdArg) = args
				var id: Int = additionalMethods.size

				while (--id >= 0) {
					val method: AstMethod = additionalMethods[id]
					val (keyClass, members) = list[id * casesPerMethod]
					IF(AstExpr.BINOP(AstType.BOOL, classIdArg.expr, AstBinop.GE, keyClass.classId.lit)) {
						val params = listOf(classIdArg.expr)
						val callExprUncasted = AstExpr.CALL_STATIC(method.ref, params)

						RETURN(callExprUncasted.castTo(ARRAY(MemberInfoClass)))
					}
				}

				RETURN(NULL)
			}

			// ProgramReflectionClass.getConstructors
			if (program.contains(ProgramReflection.AllConstructors::class.java.fqname)) {
				val allConstructorsClass: AstClass = program[ProgramReflection.AllConstructors::class.java.fqname]
				val getConstructorsMethod: AstMethod? = allConstructorsClass.getMethodWithoutOverrides(ProgramReflection.AllConstructors::getConstructors.name)

				if (getConstructorsMethod != null) {
					val list: List<Pair<AstClass, List<MemberInfoWithRef>>> = visibleClasses.map { clazz ->
						clazz to clazz.constructors.filter { it.mustReflect() }.map {
							MemberInfoWithRef(it.ref, MemberInfo(it.id, null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
						}
					}.toList()

					val additionalMethods = genMemberMethods(list, allConstructorsClass, getConstructorsMethod)
					getConstructorsMethod.replaceBodyOptBuild {
						genMemberList(it, list, additionalMethods)
					}
				}
			}

			// ProgramReflectionClass.getMethods
			if (program.contains(ProgramReflection.AllMethods::class.java.fqname)) {
				val allMethodsClass: AstClass = program[ProgramReflection.AllMethods::class.java.fqname]
				val getMethodsMethod: AstMethod? = allMethodsClass.getMethodWithoutOverrides(ProgramReflection.AllMethods::getMethods.name)

				if (getMethodsMethod != null) {
					val list: List<Pair<AstClass, List<MemberInfoWithRef>>> = visibleClasses.map { clazz ->
						clazz to clazz.methodsWithoutConstructors.filter { it.mustReflect() }.map {
							MemberInfoWithRef(it.ref, MemberInfo(it.id, null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
						}
					}.toList()

					val additionalMethods = genMemberMethods(list, allMethodsClass, getMethodsMethod)
					getMethodsMethod.replaceBodyOptBuild {
						genMemberList(it, list, additionalMethods)
					}
				}
			}

			// ProgramReflectionClass.getFields
			if (program.contains(ProgramReflection.AllFields::class.java.fqname)) {
				val allFieldsClass: AstClass = program[ProgramReflection.AllFields::class.java.fqname]
				val getFieldsMethod: AstMethod? = allFieldsClass.getMethodWithoutOverrides(ProgramReflection.AllFields::getFields.name)

				if (getFieldsMethod != null) {
					val list: List<Pair<AstClass, List<MemberInfoWithRef>>> = visibleClasses.map { clazz ->
						clazz to clazz.fields.filter { it.mustReflect() }.map {
							MemberInfoWithRef(it.ref, MemberInfo(it.id, null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
						}
					}.toList()

					val additionalMethods = genMemberMethods(list, allFieldsClass, getFieldsMethod)
					getFieldsMethod.replaceBodyOptBuild {
						genMemberList(it, list, additionalMethods)
					}
				}
			}

			// ProgramReflectionClass.dynamicGet
			if (program.contains(ProgramReflection.DynamicGetSet::class.java.fqname)) {
				val dynamicGetSetClass: AstClass = program[ProgramReflection.DynamicGetSet::class.java.fqname]
				val dynamicGetMethod: AstMethod? = dynamicGetSetClass.getMethodWithoutOverrides(ProgramReflection.DynamicGetSet::dynamicGet.name)

				if (dynamicGetMethod != null) {
					val additionalMethods: MutableList<AstMethod> = mutableListOf()
					val fields: List<AstField> = visibleClasses.flatMap { it.fields.filter { it.mustReflect() } }.sortedBy { it.id }

					var methodIndex: Int = 0
					while (methodIndex * casesPerMethod < fields.size) {
						val mI: Int = methodIndex
						val sI: Int = methodIndex * casesPerMethod

						val newMethod: AstMethod = dynamicGetSetClass.createMethod(dynamicGetMethod.name + mI, dynamicGetMethod.methodType, true) {
							val (classId, fieldId, objParam) = it
							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + casesPerMethod < fields.size) currentIndex + casesPerMethod else fields.size

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
							val field: AstField = fields[id * casesPerMethod]
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
			if (program.contains(ProgramReflection.DynamicGetSet::class.java.fqname)) {
				val dynamicGetSetClass: AstClass = program[ProgramReflection.DynamicGetSet::class.java.fqname]
				val dynamicSetMethod: AstMethod? = dynamicGetSetClass.getMethodWithoutOverrides(ProgramReflection.DynamicGetSet::dynamicSet.name)

				if (dynamicSetMethod != null) {
					val additionalMethods: MutableList<AstMethod> = mutableListOf()
					val fields: List<AstField> = visibleClasses.flatMap { it.fields.filter { it.mustReflect() } }.sortedBy { it.id }

					var methodIndex: Int = 0
					while (methodIndex * casesPerMethod < fields.size) {
						val mI: Int = methodIndex
						val sI: Int = methodIndex * casesPerMethod

						val newMethod: AstMethod = dynamicGetSetClass.createMethod(dynamicSetMethod.name + mI, dynamicSetMethod.methodType, true) {
							val (classIdParam, fieldIdParam, objParam, valueParam) = it
							var currentIndex: Int = sI
							val finishIndex: Int = if (currentIndex + casesPerMethod < fields.size) currentIndex + casesPerMethod else fields.size

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
							val field: AstField = fields[id * casesPerMethod]
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

