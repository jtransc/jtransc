package com.jtransc.meta

import com.jtransc.ast.*
import j.ClassInfo
import j.MemberInfo
import j.ProgramReflection

/**
 * This class aims to create classes to perform reflection on available classes
 */
class MetaReflectionPlugin : JTranscMetaPlugin {
	override fun process(program: AstProgram) {
		// Do not generate if ProgramReflection class is not referenced!
		// Also methods are not updated in the case they do not exist!
		if (ProgramReflection::class.java.fqname !in program) return

		val types = program.types
		val ProgramReflectionClass = program[ProgramReflection::class.java.fqname]
		val oldClasses = program.classes.toList()
		val CLASS_INFO = program[ClassInfo::class.java.fqname]
		val CLASS_INFO_CREATE = CLASS_INFO.getMethodWithoutOverrides(ClassInfo::create.name)!!.ref

		val classesToId = oldClasses.associate { it to program.getClassId(it.name) }
		val constructorsToId = oldClasses.flatMap { it.constructors }.withIndex().associate { it.value to it.index }
		val methodsToId = oldClasses.flatMap { it.methodsWithoutConstructors }.withIndex().associate { it.value to it.index }
		val fieldsToId = oldClasses.flatMap { it.fields }.withIndex().associate { it.value to it.index }

		fun getClassId(clazz: AstClass?): Int = classesToId[clazz] ?: -1
		fun getConstructorId(constructor: AstMethod?): Int = constructorsToId[constructor] ?: -1
		fun getMethodId(method: AstMethod?): Int = methodsToId[method] ?: -1
		fun getFieldId(field: AstField?): Int = fieldsToId[field] ?: -1

		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getAllClasses.name)?.replaceBodyOpt {
			val out = AstLocal(0, "out", AstTypeBuild { ARRAY(CLASS_INFO) })
			AstBuilder(types).run {
				val stms = arrayListOf<AstStm>()

				stms += out assignTo AstExpr.NEW_ARRAY(AstTypeBuild { ARRAY(CLASS_INFO) }, listOf((oldClasses.size + 1).lit))

				for (oldClass in oldClasses) {
					val index = getClassId(oldClass)

					stms += AstStm.SET_ARRAY(out.local, index.lit, AstExpr.CALL_STATIC(CLASS_INFO.ref, CLASS_INFO_CREATE, listOf(
						index.lit,
						oldClass.fqname.lit,
						oldClass.modifiers.acc.lit,
						getClassId(oldClass.parentClass).lit,
						AstExpr.INTARRAY_LITERAL(oldClass.directInterfaces.map { getClassId(it) }),
						AstExpr.INTARRAY_LITERAL(oldClass.getAllRelatedTypesIdsWith0AtEnd())
					)))
				}
				stms += AstStm.RETURN(out.local)

				stms(stms)
			}
		}

		// @TODO: We should create a submethod per class to avoid calling ::SI (static initialization) for all the classes
		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::dynamicInvoke.name)?.replaceBodyOpt {
			val methodId = AstArgument(0, AstType.INT)
			val obj = AstArgument(1, AstType.OBJECT)
			val args = AstArgument(2, AstType.ARRAY(AstType.OBJECT))
			AstBuilder(types).run {
				val cases = arrayListOf<Pair<Int, AstStm>>()

				for ((method, methodId) in methodsToId) {
					val params = method.methodType.args.map {
						cast(AstExpr.ARRAY_ACCESS(args.expr, it.index.lit), it.type)
					}

					val callExprUncasted = if (method.isStatic) {
						AstExpr.CALL_STATIC(method.containingClass.ref, method.ref, params)
					} else {
						AstExpr.CALL_INSTANCE(obj.expr, method.ref, params)
					}

					cases += methodId to if (method.methodType.retVoid) {
						stms(
							//LOGSTR("dynamicInvoke: $methodId".lit),
							AstStm.STM_EXPR(callExprUncasted), AstStm.RETURN(NULL)
						)
					} else {
						stms(
							//LOGSTR("dynamicInvoke: $methodId".lit),
							AstStm.RETURN(AstExpr.CAST(callExprUncasted, OBJECT))
						)
					}
				}

				stms(
					AstStm.SWITCH(methodId.expr, stms(), cases),
					AstStm.RETURN(NULL)
				)
			}
		}

		// @TODO: We should create a submethod per class to avoid calling ::SI (static initialization) for all the classes
		ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::dynamicNew.name)?.replaceBodyOpt {
			val methodId = AstArgument(0, AstType.INT)
			val args = AstArgument(1, AstType.ARRAY(AstType.OBJECT))
			AstBuilder(types).run {
				val cases = arrayListOf<Pair<Int, AstStm>>()

				for ((constructor, index) in constructorsToId) {
					val params = constructor.methodType.args.map {
						cast(AstExpr.ARRAY_ACCESS(args.expr, it.index.lit), it.type)
					}

					val callExprUncasted = AstExpr.NEW_WITH_CONSTRUCTOR(constructor.ref, params)

					cases += index to stms(AstStm.RETURN(AstExpr.CAST(callExprUncasted, OBJECT)))
				}

				stms(
					AstStm.SWITCH(methodId.expr, stms(), cases),
					AstStm.RETURN(NULL)
				)
			}
		}

		if (MemberInfo::class.java.fqname in program) {
			val MEMBER_INFO = program[MemberInfo::class.java.fqname]
			val MEMBER_INFO_CREATE = MEMBER_INFO.getMethodWithoutOverrides(MemberInfo::create.name)!!.ref
			val MEMBER_INFO_CREATE_LIST = MEMBER_INFO.getMethodWithoutOverrides(MemberInfo::createList.name)!!.ref

			fun genMemberList(list: Map<AstClass, List<MemberInfo>>): AstStm {
				val out = AstLocal(0, "out", AstTypeBuild { ARRAY(MEMBER_INFO) })
				val classIdArg = AstArgument(0, AstType.INT)
				return AstBuilder(types).run {
					val cases = arrayListOf<Pair<Int, AstStm>>()

					for ((clazz, members) in list) {
						val classId = getClassId(clazz)
						val stms = arrayListOf<AstStm>()
						if (members.isEmpty()) {
							stms += RETURN(NULL)
						} else {
							stms += out assignTo AstExpr.NEW_ARRAY(AstTypeBuild { ARRAY(MEMBER_INFO) }, listOf(members.size.lit))
							for ((index, member) in members.withIndex()) {
								// static public MemberInfo create(int id, String name, int modifiers, String desc, String genericDesc)
								val call = AstExpr.CALL_STATIC(MEMBER_INFO_CREATE.containingClassType, MEMBER_INFO_CREATE, listOf(
									member.id.lit,
									member.name.lit,
									member.modifiers.lit,
									member.desc.lit,
									member.genericDesc.lit
								))

								stms += AstStm.SET_ARRAY(out.local, index.lit, call)
								//field.
							}
							stms += RETURN(out.local)
						}
						//getFieldId()
						cases += classId to stms(stms)
					}

					stms(
						AstStm.SWITCH(classIdArg.expr, stms(RETURN(NULL)), cases),
						RETURN(NULL)
					)
				}
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getConstructors.name)?.replaceBodyOpt {
				genMemberList(classesToId.mapValues { clazzPair ->
					val clazz = clazzPair.key
					val classId = getClassId(clazz)
					clazz.constructors.map {
						MemberInfo(getConstructorId(it), it.name, it.modifiers.acc, it.desc, it.genericSignature)
					}
				})
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getMethods.name)?.replaceBodyOpt {
				genMemberList(classesToId.mapValues { clazzPair ->
					val clazz = clazzPair.key
					val classId = getClassId(clazz)
					clazz.methodsWithoutConstructors.map {
						MemberInfo(getMethodId(it), it.name, it.modifiers.acc, it.desc, it.genericSignature)
					}
				})
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::getFields.name)?.replaceBodyOpt {
				genMemberList(classesToId.mapValues { clazzPair ->
					val clazz = clazzPair.key
					val classId = getClassId(clazz)
					clazz.fields.map {
						MemberInfo(getFieldId(it), it.name, it.modifiers.acc, it.desc, it.genericSignature)
					}
				})
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::dynamicGet.name)?.replaceBodyOpt {
				val fieldIdParam = AstArgument(0, AstType.INT)
				val objParam = AstArgument(1, AstType.OBJECT)

				AstBuilder(types).run {
					val cases = arrayListOf<Pair<Int, AstStm>>()

					for ((field, fieldId) in fieldsToId) {
						val expr = if (field.isStatic) {
							AstExpr.FIELD_STATIC_ACCESS(field.ref)
						} else {
							AstExpr.FIELD_INSTANCE_ACCESS(field.ref, objParam.expr)
						}
						cases += fieldId to RETURN(AstExpr.CAST(expr, OBJECT))
					}

					stms(
						AstStm.SWITCH(fieldIdParam.expr, stms(), cases),
						RETURN(NULL)
					)
				}
			}

			ProgramReflectionClass.getMethodWithoutOverrides(ProgramReflection::dynamicSet.name)?.replaceBodyOpt {
				val fieldIdParam = AstArgument(0, AstType.INT)
				val objParam = AstArgument(1, AstType.OBJECT)
				val valueParam = AstArgument(2, AstType.OBJECT)

				AstBuilder(types).run {
					val cases = arrayListOf<Pair<Int, AstStm>>()

					for ((field, fieldId) in fieldsToId) {
						val expr = AstExpr.CAST(valueParam.expr, field.type)
						val stm = if (field.isStatic) {
							AstStm.SET_FIELD_STATIC(field.ref, expr)
						} else {
							AstStm.SET_FIELD_INSTANCE(field.ref, objParam.expr, expr)
						}
						cases += fieldId to stm
					}

					stms(
						AstStm.SWITCH(fieldIdParam.expr, stms(), cases),
						RETURN()
					)
				}
			}
		}
	}
}

