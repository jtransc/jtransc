package com.jtransc.gen.common

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.injector.Singleton
import com.jtransc.lang.high
import com.jtransc.lang.low
import com.jtransc.text.escape
import com.jtransc.text.isLetterDigitOrUnderscore
import com.jtransc.text.quote
import kotlin.reflect.KMutableProperty1

@Singleton
abstract class CommonNames(
	val program: AstResolver,
	val keywords: Set<String> = setOf()
) {
	lateinit var currentClass: FqName
	lateinit var currentMethod: AstMethodRef
	enum class StringPoolType { GLOBAL, PER_CLASS }

	abstract val stringPoolType: StringPoolType

	class StringPool {
		private var lastId = 0
		private val stringIds = hashMapOf<String, Int>()
		private var valid = false
		private var cachedEntries = listOf<StringInPool>()
		fun alloc(str: String): Int {
			return stringIds.getOrPut(str) {
				valid = false
				lastId++
			}
		}

		fun getAllSorted(): List<StringInPool> {
			if (!valid) {
				cachedEntries = stringIds.entries.map { StringInPool(it.value, it.key) }.sortedBy { it.id }.toList()
				valid = true
			}
			return cachedEntries
		}
	}

	class PerClassNameAllocator {
		val usedNames = hashSetOf<String>()
		val allocatedNames = hashMapOf<Any, String>()

		fun allocate(key: Any, requestedName: () -> String): String {
			if (key !in allocatedNames) {
				var finalName = requestedName()
				while (finalName in usedNames) {
					finalName += "_"
				}
				usedNames += finalName
				allocatedNames[key] = finalName
			}
			return allocatedNames[key]!!
		}
	}

	val perClassNameAllocator = hashMapOf<FqName, PerClassNameAllocator>()

	private val stringPoolGlobal = StringPool()
	private val stringPoolPerClass = hashMapOf<FqName, StringPool>()

	data class StringInPool(val id: Int, val str: String) {
		val name = "STRINGLIT_$id"
	}

	fun getClassNameAllocator(clazz: FqName) = perClassNameAllocator.getOrPut(clazz) { PerClassNameAllocator() }

	private fun getPerClassStrings(clazz: FqName) = stringPoolPerClass.getOrPut(clazz) { StringPool() }

	fun getGlobalStrings(): List<StringInPool> = when (stringPoolType) {
		StringPoolType.GLOBAL -> stringPoolGlobal.getAllSorted()
		else -> invalidOp("This target doesn't support global string pool")
	}

	fun getClassStrings(clazz: FqName): List<StringInPool> = when (stringPoolType) {
		StringPoolType.PER_CLASS -> getPerClassStrings(clazz).getAllSorted()
		else -> invalidOp("This target doesn't support per class string pool")
	}

	fun allocString(clazz: FqName, str: String): Int = when (stringPoolType) {
		StringPoolType.GLOBAL -> stringPoolGlobal.alloc(str)
		StringPoolType.PER_CLASS -> getPerClassStrings(clazz).alloc(str)
	}

	open fun buildTemplateClass(clazz: FqName): String = getClassFqNameForCalling(clazz)
	open fun buildTemplateClass(clazz: AstClass): String = getClassFqNameForCalling(clazz.name)

	fun buildField(field: AstField, static: Boolean): String {
		return if (static) buildStaticField(field) else getNativeName(field)
	}

	open fun buildMethod(method: AstMethod, static: Boolean): String {
		val clazz = getClassFqNameForCalling(method.containingClass.name)
		val name = getNativeName(method)
		return if (static) ("$clazz" + buildAccessName(name, static = true)) else name
	}

	open fun buildStaticInit(clazz: AstClass): String {
		//getClassStaticInit(clazz.ref, "template sinit")
		return getClassFqNameForCalling(clazz.name) + buildAccessName("SI", static = true) + "();"
	}

	open fun buildConstructor(method: AstMethod): String {
		val clazz = getClassFqNameForCalling(method.containingClass.name)
		val methodName = getNativeName(method)
		return "new $clazz()[${methodName.quote()}]"
	}

	open fun getClassStaticInit(classRef: AstType.REF, reason: String): String = buildStaticInit(program[classRef.name]!!)

	open fun getClassFqName(name: FqName): String = name.fqname
	open fun getFilePath(name: FqName): String = name.simpleName

	open fun buildInstanceField(expr: String, field: AstField): String = expr + buildAccessName(field, static = false)
	open fun buildStaticField(field: AstField): String = getNativeNameForFields(field.ref.containingTypeRef.name) + buildAccessName(field, static = true)

	fun buildStaticField(field: FieldRef): String = buildStaticField(program[field.ref]!!)
	fun buildInstanceField(expr: String, field: FieldRef): String = buildInstanceField(expr, program[field.ref]!!)

	open fun buildAccessName(field: AstField, static:Boolean): String = buildAccessName(getNativeName(field), static)
	open fun buildAccessName(name: String, static:Boolean): String = ".$name"

	val normalizeNameCache = hashMapOf<String, String>()

	fun normalizeName(name: String): String {
		if (name.isNullOrEmpty()) return ""
		if (name !in normalizeNameCache) {
			val chars = name.toCharArray()
			for (i in chars.indices) {
				var c = chars[i]
				if (!c.isLetterDigitOrUnderscore() || c == '$') c = '_'
				chars[i] = c
			}
			if (chars[0].isDigit()) chars[0] = '_'
			normalizeNameCache[name] = String(chars)
		}
		return normalizeNameCache[name]!!
	}

	open fun getNativeName(local: LocalParamRef): String = normalizeName(local.name)
	open fun getNativeName(field: FieldRef): String = normalizeName(field.ref.name)
	open fun getNativeName(method: MethodRef): String = normalizeName(method.ref.name)
	open fun getNativeName(clazz: FqName): String = getClassFqNameForCalling(clazz)

	inline fun <reified T : Any> nativeName(): String = getNativeName(T::class.java.name.fqname)

	fun getNativeFieldName(clazz: Class<*>, name: String): String {
		val actualClazz = program[clazz.name.fqname] ?: invalidOp("Can't find field $clazz.$name")
		val actualField = actualClazz.fieldsByName[name] ?: invalidOp("Can't find field $clazz.$name")
		return getNativeName(actualField)
	}

	inline fun <reified T : Any, R> getNativeFieldName(prop: KMutableProperty1<T, R>): String {
		return getNativeFieldName(T::class.java, prop.name)
	}

	open fun getNativeNameForMethods(clazz: FqName): String = getNativeName(clazz)
	open fun getNativeNameForFields(clazz: FqName): String = getNativeName(clazz)

	open val NullType = FqName("Dynamic")
	open val VoidType = FqName("Void")
	open val BoolType = FqName("Bool")
	open val IntType = FqName("Int")
	open val FloatType = FqName("Float32")
	open val DoubleType = FqName("Float64")
	open val LongType = FqName("haxe.Int64")
	open val BaseArrayType = FqName("JA_0")
	open val BoolArrayType = FqName("JA_Z")
	open val ByteArrayType = FqName("JA_B")
	open val CharArrayType = FqName("JA_C")
	open val ShortArrayType = FqName("JA_S")
	open val IntArrayType = FqName("JA_I")
	open val LongArrayType = FqName("JA_J")
	open val FloatArrayType = FqName("JA_F")
	open val DoubleArrayType = FqName("JA_D")
	open val ObjectArrayType = FqName("JA_L")

	open fun getDefault(type: AstType): Any? = type.getNull()

	open fun getNativeType(type: AstType, typeKind: GenCommonGen.TypeKind): FqName {
		return when (type) {
			is AstType.NULL -> NullType
			is AstType.VOID -> VoidType
			is AstType.BOOL -> BoolType
			is AstType.GENERIC -> getNativeType(type.type, typeKind)
			is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> IntType
			is AstType.FLOAT -> FloatType
			is AstType.DOUBLE -> DoubleType
			is AstType.LONG -> LongType
			is AstType.REF -> FqName(program[type.name]?.nativeName ?: getNativeName(type.name))
			is AstType.ARRAY -> when (type.element) {
				is AstType.BOOL -> BoolArrayType
				is AstType.BYTE -> ByteArrayType
				is AstType.CHAR -> CharArrayType
				is AstType.SHORT -> ShortArrayType
				is AstType.INT -> IntArrayType
				is AstType.LONG -> LongArrayType
				is AstType.FLOAT -> FloatArrayType
				is AstType.DOUBLE -> DoubleArrayType
				else -> ObjectArrayType
			}
			else -> throw RuntimeException("Not supported native type $type, $typeKind")
		}
	}

	open fun escapeConstantRef(value: Any?, type: AstType): String {
		return when (value) {
			is Long -> N_func("lnewRef", "${value.high}, ${value.low}")
			else -> escapeConstant(value, type)
		}
	}

	open fun escapeConstant(value: Any?, type: AstType): String {
		val result = escapeConstant(value)
		return if (type != AstType.BOOL) result else if (result != "false" && result != "0") "true" else "false"
	}

	open val staticAccessOperator: String = "."
	open val instanceAccessOperator: String = "."

	open fun N_lnew(value:Long) = N_func("lnew", "${value.high}, ${value.low}")

	open fun escapeConstant(value: Any?): String = when (value) {
		null -> "null"
		is Boolean -> if (value) "true" else "false"
		is String -> N_func("strLitEscape", value.quote())
		is Long -> N_lnew(value)
		is Float -> escapeConstant(value.toDouble())
		is Double -> if (value.isInfinite()) if (value < 0) NegativeInfinityString else PositiveInfinityString else if (value.isNaN()) NanString else "$value"
		is Int -> when (value) {
			Int.MIN_VALUE -> "N${staticAccessOperator}MIN_INT32"
			else -> "$value"
		}
		is Number -> "${value.toInt()}"
		is Char -> "${value.toInt()}"
		is AstType -> N_func("resolveClass", "${value.mangle().quote()}")
		else -> throw NotImplementedError("Literal of type $value")
	}

	open val NegativeInfinityString = "-Infinity"
	open val PositiveInfinityString = "Infinity"
	open val NanString = "NaN"

	open fun getClassFqNameForCalling(fqName: FqName): String = fqName.fqname.replace('.', '_')

	open fun getGeneratedFqName(name: FqName): FqName = name
	open fun getGeneratedSimpleClassName(name: FqName): String = name.fqname

	fun getFieldName(clazz: Class<*>, name: String): String = getFieldName(program[clazz.name.fqname]!!.fieldsByName[name]!!)
	fun getFieldName(field: FieldRef): String = getFieldName(field.ref)
	fun getFieldName(field: AstField) = getFieldName(field.ref)
	open fun getFieldName(field: AstFieldRef): String = field.name

	open fun getClassFqNameLambda(name: FqName): String {
		val clazz = program[name]
		val simpleName = getGeneratedSimpleClassName(name)
		return getClassFqName(clazz?.name ?: name) + ".${simpleName}_Lambda"
	}

	open fun getClassFqNameInt(name: FqName): String {
		val clazz = program[name]
		val simpleName = getGeneratedSimpleClassName(name)
		val suffix = if (clazz?.isInterface ?: false) ".${simpleName}_IFields" else ""
		return getClassFqName(clazz?.name ?: name) + "$suffix"
	}

	open fun getGeneratedFqPackage(name: FqName): String = name.packagePath

	open fun getFunctionalType(type: AstType.METHOD): String {
		return type.argsPlusReturnVoidIsEmpty.map { getNativeType(it, GenCommonGen.TypeKind.TYPETAG) }.joinToString(" -> ")
	}

	open fun getAnnotationProxyName(classRef: AstType.REF): String = "AnnotationProxy_${getGeneratedFqName(classRef.name).fqname.replace('.', '_')}"

	open fun getFullAnnotationProxyName(classRef: AstType.REF): String {
		return getClassFqName(classRef.name) + ".AnnotationProxy_${getGeneratedFqName(classRef.name).fqname.replace('.', '_')}"
	}

	open fun getClassStaticClassInit(classRef: AstType.REF): String = "${getClassFqNameInt(classRef.name)}.HAXE_CLASS_INIT"

	open fun getTargetMethodAccess(refMethod: AstMethod, static: Boolean): String = buildAccessName(getNativeName(refMethod), static)

	open fun getTypeStringForCpp(type: AstType): String = noImpl

	fun N_func(name: String, args: String) = "N$staticAccessOperator$name($args)"
}