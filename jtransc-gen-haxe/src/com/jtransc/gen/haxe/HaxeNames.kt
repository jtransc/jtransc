package com.jtransc.gen.haxe

import com.jtransc.ast.*
import com.jtransc.ds.getOrPut2
import com.jtransc.error.invalidOp
import com.jtransc.error.unexpected
import com.jtransc.gen.MinimizedNames
import com.jtransc.gen.common.CommonGenGen
import com.jtransc.gen.common.CommonNames
import com.jtransc.text.escape
import com.jtransc.text.quote

class HaxeNames(
	program: AstResolver,
	val minimize: Boolean = false
) : CommonNames(program) {
	companion object {
		val HaxeKeywords = setOf(
			"haxe",
			"Dynamic",
			"Void",
			"java",
			"package",
			"import",
			"class", "interface", "extends", "implements",
			"internal", "private", "protected", "final",
			"function", "var", "const",
			"if", "else",
			"switch", "case", "default",
			"do", "while", "for", "each", "in",
			"try", "catch", "finally",
			"break", "continue",
			"int", "uint", "void",
			"goto"
		)

		val HaxeSpecial = setOf(
			"hx",
			"z", // used for package
			"N", // used for HaxeNatives
			"NN", // used for HaxeNatives without references to other classes
			"R", // used for reflect
			"SI", // STATIC INIT
			"SII", // STATIC INIT INITIALIZED
			"HAXE_CLASS_INIT", // Information about the class
			"HAXE_CLASS_NAME", // Information about the class
			"HaxeNatives", // used for HaxeNatives
			"unix",
			"OVERFLOW", // iphone sdk
			"UNDERFLOW" // iphone sdk
		)

		val HaxeKeywordsWithToStringAndHashCode: Set<String> = HaxeKeywords + HaxeSpecial + setOf("toString", "hashCode")
	}

	override fun buildConstructor(method: AstMethod): String = "new ${getHaxeClassFqName(method.containingClass.name)}().${getHaxeMethodName(method)}"

	override fun buildStaticInit(clazz: AstClass): String = getHaxeClassStaticInit(clazz.ref, "template sinit")

	override fun buildMethod(method: AstMethod, static: Boolean): String {
		val clazz = getHaxeClassFqName(method.containingClass.name)
		val name = getHaxeMethodName(method)
		return if (static) "$clazz.$name" else "$name"
	}

	override fun getNativeName(local: LocalParamRef): String = super.getNativeName(local)
	override fun getNativeName(field: FieldRef): String = getHaxeFieldName(field)
	override fun getNativeName(method: MethodRef): String = getHaxeMethodName(method.ref)
	override fun getNativeName(clazz: FqName): String = getHaxeClassFqName(clazz)
	override fun getNativeNameForFields(clazz: FqName): String = getHaxeClassFqNameInt(clazz)

	override fun buildTemplateClass(clazz: FqName): String = getHaxeClassFqName(clazz)
	override fun buildTemplateClass(clazz: AstClass): String = getHaxeClassFqName(clazz.name)
	private val cachedFieldNames = hashMapOf<AstFieldRef, String>()

	//val ENABLED_MINIFY = false
	val ENABLED_MINIFY = true
	private val ENABLED_MINIFY_MEMBERS = ENABLED_MINIFY && minimize
	private val ENABLED_MINIFY_CLASSES = ENABLED_MINIFY && minimize

	//private val ENABLED_MINIFY_CLASSES = true
	//private val ENABLED_MINIFY_MEMBERS = false

	private var minClassLastId: Int = 0
	private var minMemberLastId: Int = 0
	private val classNames = hashMapOf<FqName, FqName>()
	private val methodNames = hashMapOf<Any?, String>()
	private val fieldNames = hashMapOf<Any?, String>()

	val minClassPrefix = "z."
	//val minClassPrefix = ""

	private fun <T> Set<T>.runUntilNotInSet(callback: () -> T): T {
		while (true) {
			val result = callback()
			if (result !in this) return result
		}
	}

	fun allocClassName(): String = HaxeKeywordsWithToStringAndHashCode.runUntilNotInSet { MinimizedNames.getTypeNameById(minClassLastId++) }
	fun allocMemberName(): String = HaxeKeywordsWithToStringAndHashCode.runUntilNotInSet { MinimizedNames.getIdNameById(minMemberLastId++) }

	fun getHaxeMethodName(method: AstMethod): String = getHaxeMethodName(method.ref)
	fun getHaxeMethodName(method: AstMethodRef): String {
		val realmethod = program[method] ?: invalidOp("Can't find method $method")
		val realclass = realmethod.containingClass
		val methodWithoutClass = method.withoutClass

		val objectToCache: Any = if (method.isClassOrInstanceInit) method else methodWithoutClass

		return if (realclass.isNative) {
			// No cache
			realmethod.nativeName ?: method.name
		} else {
			methodNames.getOrPut2(objectToCache) {
				if (ENABLED_MINIFY_MEMBERS && !realmethod.keepName) {
					allocMemberName()
				} else {
					if (realmethod.nativeMethod != null) {
						realmethod.nativeMethod!!
					} else {
						val name2 = "${method.name}${method.desc}"
						val name = when (method.name) {
							"<init>", "<clinit>" -> "${method.containingClass}$name2"
							else -> name2
						}
						cleanName(name)
					}
				}
			}
		}
	}

	private fun cleanName(name: String): String {
		val out = CharArray(name.length)
		for (n in 0 until name.length) out[n] = if (name[n].isLetterOrDigit()) name[n] else '_'
		return String(out)
	}

	fun getHaxeFunctionalType(type: AstType.METHOD): String {
		return type.argsPlusReturnVoidIsEmpty.map { getNativeType(it, CommonGenGen.TypeKind.TYPETAG) }.joinToString(" -> ")
	}

	fun getHaxeDefault(type: AstType): Any? = type.getNull()

	private fun _getHaxeFqName(name: FqName): FqName {
		val realclass = if (name in program) program[name]!! else null
		return classNames.getOrPut2(name) {
			if (realclass?.nativeName != null) {
				FqName(realclass!!.nativeName!!)
			} else if (ENABLED_MINIFY_CLASSES && !realclass.keepName) {
				FqName(minClassPrefix + allocClassName())
			} else {
				FqName(name.packageParts.map { if (it in HaxeKeywords) "${it}_" else it }.map { it.decapitalize() }, "${name.simpleName.replace('$', '_')}_".capitalize())
			}
		}
	}

	fun getHaxeFilePath(name: FqName): String = getHaxeGeneratedFqName(name).internalFqname + ".hx"

	fun getHaxeGeneratedFqPackage(name: FqName): String = _getHaxeFqName(name).packagePath
	fun getHaxeGeneratedFqName(name: FqName): FqName = _getHaxeFqName(name)
	fun getHaxeGeneratedSimpleClassName(name: FqName): String = _getHaxeFqName(name).simpleName
	inline fun <reified T : Any> haxeName(): String = getHaxeClassFqName(T::class.java.name.fqname)

	fun getHaxeClassFqName(name: FqName): String {
		val clazz = if (name in program) program[name] else null
		return clazz?.nativeName ?: getHaxeGeneratedFqName(name).fqname
	}

	fun getHaxeFieldName(clazz: Class<*>, name: String): String {
		return getHaxeFieldName(program[clazz.name.fqname]!!.fieldsByName[name]!!)
	}

	fun getHaxeFieldName(field: FieldRef): String = getHaxeFieldName(field.ref)

	fun getHaxeFieldName(field: AstFieldRef): String {
		val realfield = program[field]
		val realclass = program[field.containingClass]
		//val keyToUse = if (realfield.keepName) field else field.name
		//val keyToUse = if (ENABLED_MINIFY_FIELDS) field else field.name
		val keyToUse = field

		//if (field.name == "static,strange,field" || field.name == "static,strange=field") {
		//	println("strange! : ${field.name}")
		//	println("-")
		//}

		val normalizedFieldName = normalizeName(field.name)

		return if (realclass.isNative) {
			// No cache
			realfield?.nativeName ?: normalizedFieldName
		} else {
			fieldNames.getOrPut2(keyToUse) {
				if (ENABLED_MINIFY_MEMBERS && !realfield.keepName) {
					allocMemberName()
				} else {
					if (field !in cachedFieldNames) {
						val fieldName = normalizedFieldName
						var name = if (fieldName in HaxeKeywordsWithToStringAndHashCode) "${fieldName}_" else fieldName

						val clazz = program[field]?.containingClass
						val clazzAncestors = clazz?.ancestors?.reversed() ?: listOf()
						val names = clazzAncestors.flatMap { it.fields }.filter { it.name == field.name }.map { getHaxeFieldName(it.ref) }.toHashSet()
						val fieldsColliding = clazz?.fields?.filter {
							(it.ref == field) || (normalizeName(it.name) == normalizedFieldName)
						}?.map { it.ref } ?: listOf(field)

						// JTranscBugInnerMethodsWithSameName.kt
						for (f2 in fieldsColliding) {
							while (name in names) name += "_"
							cachedFieldNames[f2] = name
							names += name
						}
						cachedFieldNames[field] ?: unexpected("Unexpected. Not cached: $field")
					}
					cachedFieldNames[field] ?: unexpected("Unexpected. Not cached: $field")
				}
			}
		}
	}

	fun getHaxeFieldName(field: AstField) = getHaxeFieldName(field.ref)


	fun getHaxeClassFqNameInt(name: FqName): String {
		val clazz = program[name]
		val simpleName = getHaxeGeneratedSimpleClassName(name)
		val suffix = if (clazz?.isInterface ?: false) ".${simpleName}_IFields" else ""
		return getHaxeClassFqName(clazz?.name ?: name) + "$suffix"
	}

	fun getHaxeClassFqNameLambda(name: FqName): String {
		val clazz = program[name]
		val simpleName = getHaxeGeneratedSimpleClassName(name)
		return getHaxeClassFqName(clazz?.name ?: name) + ".${simpleName}_Lambda"
	}

	fun getHaxeClassStaticInit(classRef: AstType.REF, reason: String): String {
		val clazz = program[classRef.name]
		if (clazz?.nativeName != null) {
			return ""
		} else {
			return "${getHaxeClassFqNameInt(classRef.name)}.SI() /* $reason */;"
		}
	}

	fun getHaxeClassStaticClassInit(classRef: AstType.REF): String {
		return "${getHaxeClassFqNameInt(classRef.name)}.HAXE_CLASS_INIT"
	}

	fun getAnnotationProxyName(classRef: AstType.REF): String {
		return "AnnotationProxy_${getHaxeGeneratedFqName(classRef.name).fqname.replace('.', '_')}"
	}

	fun getFullAnnotationProxyName(classRef: AstType.REF): String {
		return getHaxeClassFqName(classRef.name) + ".AnnotationProxy_${getHaxeGeneratedFqName(classRef.name).fqname.replace('.', '_')}"
	}

	override val NullType = FqName("Dynamic")
	override val VoidType = FqName("Void")
	override val BoolType = FqName("Bool")
	override val IntType = FqName("Int")
	override val FloatType = FqName("Float32")
	override val DoubleType = FqName("Float64")
	override val LongType = FqName("haxe.Int64")
	override val BaseArrayType = FqName("JA_0")
	override val BoolArrayType = FqName("JA_Z")
	override val ByteArrayType = FqName("JA_B")
	override val CharArrayType = FqName("JA_C")
	override val ShortArrayType = FqName("JA_S")
	override val IntArrayType = FqName("JA_I")
	override val LongArrayType = FqName("JA_J")
	override val FloatArrayType = FqName("JA_F")
	override val DoubleArrayType = FqName("JA_D")
	override val ObjectArrayType = FqName("JA_L")

	val HaxeArrayAny = ObjectArrayType
	val HaxeArrayBase = BaseArrayType

	override val NegativeInfinityString = "Math.NEGATIVE_INFINITY"
	override val PositiveInfinityString = "Math.POSITIVE_INFINITY"
	override val NanString = "Math.NaN"
}