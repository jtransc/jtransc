package com.jtransc.plugin.meta

import com.jtransc.annotation.JTranscInvisible
import com.jtransc.ast.*
import com.jtransc.plugin.JTranscPlugin
import j.ClassInfo
import j.MemberInfo
import j.ProgramReflection

/**
 * This class aims to create classes to perform reflection on available classes
 */
class MetaReflectionJTranscPlugin : JTranscPlugin() {
	override fun processAfterTreeShaking(program: AstProgram) {
		// Do not generate if ProgramReflection class is not referenced!
		// Also methods are not updated in the case they do not exist!
		if (ProgramReflection::class.java.fqname !in program) return
		if (ClassInfo::class.java.fqname !in program) return

		val types = program.types
		val ProgramReflectionClass = program[ProgramReflection::class.java.fqname]
		val oldClasses = program.classes.toList().filter { !it.annotationsList.contains<JTranscInvisible>() }
		val CLASS_INFO = program[ClassInfo::class.java.fqname]
		val CLASS_INFO_CREATE = CLASS_INFO.getMethodWithoutOverrides(ClassInfo::create.name)!!.ref

		val classesToId = oldClasses.associate { it to program.getClassId(it.name) }
		val constructorsToId = oldClasses.flatMap { it.constructors }.withIndex().associate { it.value to it.index }
		val methodsToId = oldClasses.flatMap { it.methodsWithoutConstructors }.filter { !it.annotationsList.contains<JTranscInvisible>() }.withIndex().associate { it.value to it.index }
		val fieldsToId = oldClasses.flatMap { it.fields }.filter { !it.annotationsList.contains<JTranscInvisible>() }.withIndex().associate { it.value to it.index }

		fun getClassId(clazz: AstClass?): Int = classesToId[clazz] ?: -1
		fun getConstructorId(constructor: AstMethod?): Int = constructorsToId[constructor] ?: -1
		fun getMethodId(method: AstMethod?): Int = methodsToId[method] ?: -1
		fun getFieldId(field: AstField?): Int = fieldsToId[field] ?: -1

		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getAllClasses.name)?.replaceBodyOptBuild {
			val out = AstLocal(0, "out", ARRAY(CLASS_INFO))

			SET(out, NEW_ARRAY(ARRAY(CLASS_INFO), (oldClasses.size + 1).lit))

			for (oldClass in oldClasses) {
				val index = getClassId(oldClass)

				SET_ARRAY(out, index.lit, CLASS_INFO_CREATE(
					index.lit,
					AstExpr.LITERAL_REFNAME(oldClass.ref, types),
					oldClass.fqname.lit,
					oldClass.modifiers.acc.lit,
					getClassId(oldClass.parentClass).lit,
					AstExpr.INTARRAY_LITERAL(oldClass.directInterfaces.map { getClassId(it) }),
					AstExpr.INTARRAY_LITERAL(oldClass.getAllRelatedTypesIdsWith0AtEnd())
				))
			}
			RETURN(out.local)

		}

		// @TODO: We should create a submethod per class to avoid calling ::SI (static initialization) for all the classes
		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::dynamicInvoke.name)?.replaceBodyOptBuild {
			val methodId = AstArgument(0, AstType.INT)
			val obj = AstArgument(1, AstType.OBJECT)
			val args = AstArgument(2, AstType.ARRAY(AstType.OBJECT))

			SWITCH(methodId.expr) {
				for ((method, methodId) in methodsToId) {
					val params = method.methodType.args.map {
						cast(AstExpr.ARRAY_ACCESS(args.expr, it.index.lit), it.type)
					}

					val callExprUncasted = if (method.isStatic) {
						AstExpr.CALL_STATIC(method.containingClass.ref, method.ref, params)
					} else {
						AstExpr.CALL_INSTANCE(obj.expr, method.ref, params)
					}

					CASE(methodId) {
						if (method.methodType.retVoid) {
							STM(callExprUncasted)
							RETURN(NULL)
						} else {
							//LOGSTR("dynamicInvoke: $methodId".lit),
							AstStm.RETURN(AstExpr.CAST(callExprUncasted, OBJECT))
						}
					}
				}
			}

			RETURN(NULL)
		}

		// @TODO: We should create a submethod per class to avoid calling ::SI (static initialization) for all the classes
		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::dynamicNew.name)?.replaceBodyOptBuild {
			val methodId = AstArgument(0, AstType.INT)
			val args = AstArgument(1, AstType.ARRAY(AstType.OBJECT))

			SWITCH(methodId.expr) {
				for ((constructor, index) in constructorsToId) {
					CASE(index) {
						val params = constructor.methodType.args.map {
							cast(AstExpr.ARRAY_ACCESS(args.expr, it.index.lit), it.type)
						}

						val callExprUncasted = AstExpr.NEW_WITH_CONSTRUCTOR(constructor.ref, params)

						AstStm.RETURN(AstExpr.CAST(callExprUncasted, OBJECT))
					}
				}
			}

			RETURN(NULL)
		}

		if (MemberInfo::class.java.fqname in program) {
			val MemberInfoClass = program[MemberInfo::class.java.fqname]
			val MemberInfo_create = MemberInfoClass.getMethodWithoutOverrides(MemberInfo::create.name)!!.ref
			//val MemberInfo_createList = MemberInfoClass.getMethodWithoutOverrides(MemberInfo::createList.name)!!.ref

			data class MemberInfoWithRef(val ref: Any, val mi: MemberInfo)

			fun AstBuilder2.genMemberList(list: Map<AstClass, List<MemberInfoWithRef>>) {
				val out = AstLocal(0, "out", ARRAY(MemberInfoClass))
				val classIdArg = AstArgument(0, AstType.INT)
				SWITCH(classIdArg.expr) {
					for ((clazz, members) in list) {
						val classId = getClassId(clazz)
						CASE(classId) {
							if (members.isEmpty()) {
								RETURN(NULL)
							} else {
								SET(out, NEW_ARRAY(ARRAY(MemberInfoClass), members.size.lit))

								for ((index, memberWithRef) in members.withIndex()) {
									val ref = memberWithRef.ref
									val member = memberWithRef.mi
									// static public MemberInfo create(int id, String name, int modifiers, String desc, String genericDesc)

									SET_ARRAY(out, index.lit,  MemberInfo_create(
										member.id.lit,
										AstExpr.LITERAL_REFNAME(ref, types),
										member.name.lit,
										member.modifiers.lit,
										member.desc.lit,
										member.genericDesc.lit
									))
								}
								RETURN(out.local)
							}
						}
					}

					DEFAULT {
						RETURN(NULL)
					}
				}
				RETURN(NULL)
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getConstructors.name)?.replaceBodyOptBuild {
				genMemberList(classesToId.mapValues { clazzPair ->
					val clazz = clazzPair.key
					//val classId = getClassId(clazz)
					clazz.constructors.map {
						MemberInfoWithRef(it.ref, MemberInfo(getConstructorId(it), null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
					}
				})
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getMethods.name)?.replaceBodyOptBuild {
				genMemberList(classesToId.mapValues { clazzPair ->
					val clazz = clazzPair.key
					//val classId = getClassId(clazz)
					clazz.methodsWithoutConstructors.map {
						MemberInfoWithRef(it.ref, MemberInfo(getMethodId(it), null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
					}
				})
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getFields.name)?.replaceBodyOptBuild {
				genMemberList(classesToId.mapValues { clazzPair ->
					val clazz = clazzPair.key
					clazz.fields.map {
						MemberInfoWithRef(it.ref, MemberInfo(getFieldId(it), null, it.name, it.modifiers.acc, it.desc, it.genericSignature))
					}
				})
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::dynamicGet.name)?.replaceBodyOptBuild {
				val fieldIdParam = AstArgument(0, AstType.INT)
				val objParam = AstArgument(1, AstType.OBJECT)

				SWITCH(fieldIdParam.expr) {
					for ((field, fieldId) in fieldsToId) {
						val expr = if (field.isStatic) {
							AstExpr.FIELD_STATIC_ACCESS(field.ref)
						} else {
							AstExpr.FIELD_INSTANCE_ACCESS(field.ref, objParam.expr)
						}

						CASE(fieldId) {
							RETURN(expr.cast(OBJECT))
						}
					}
				}

				RETURN(NULL)
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::dynamicSet.name)?.replaceBodyOptBuild {
				val fieldIdParam = AstArgument(0, AstType.INT)
				val objParam = AstArgument(1, AstType.OBJECT)
				val valueParam = AstArgument(2, AstType.OBJECT)

				SWITCH(fieldIdParam.expr) {
					for ((field, fieldId) in fieldsToId) {
						val expr = AstExpr.CAST(valueParam.expr, field.type)

						CASE(fieldId) {
							if (field.isStatic) {
								STM(AstStm.SET_FIELD_STATIC(field.ref, expr))
							} else {
								STM(AstStm.SET_FIELD_INSTANCE(field.ref, objParam.expr, expr))
							}
						}
					}
				}

				RETURN()
			}
		}
	}
}

