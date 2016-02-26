package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.ds.zipped
import com.jtransc.env.OS
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.time.measureTime
import com.jtransc.vfs.MergedLocalAndJars
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.UserKey
import soot.*
import soot.jimple.*
import soot.options.Options
import soot.tagkit.*
import java.io.File
import java.util.*

class SootToAst {
	companion object {
		fun createProgramAst(classNames: List<String>, mainClass: String, classPaths: List<String>, outputPath: SyncVfsFile, deps2: Set<AstRef>? = null): AstProgram {
			SootUtils.init(classPaths)
			return SootToAst().generateProgram(BaseProjectContext(classNames, mainClass, classPaths, outputPath, deps2?.toHashSet()))
		}

		fun checkIfClassExists(name: FqName): Boolean {
			try {
				return Scene.v().getSootClass(name.fqname) != null
			} catch (t: Throwable) {
				return false
			}
		}
	}

	fun generateProgram(projectContext: BaseProjectContext): AstProgram {
		class SootAnalyzer : ProgramAnalyzer {
		}

		val program = AstProgram(
			entrypoint = FqName(projectContext.mainClass),
			resourcesVfs = MergedLocalAndJars(projectContext.classPaths),
			analyzer = SootAnalyzer()
		)
		val tree = projectContext.tree
		program[BaseProjectContext.KEY] = projectContext

		// Preprocesses classes
		projectContext.classNames.forEach { tree.getSootClass(it.fqname) }

		print("Processing classes...")

		projectContext.classNames.forEach { tree.addClassToGenerate(it) }

		val (elapsed) = measureTime {
			while (tree.hasClassToGenerate()) {
				val className = tree.getClassToGenerate()
				val clazz = tree.getSootClass(className)
				//val nativeClassTag = clazz.clazz.getTag("libcore.NativeClass", "")

				//print("Processing class: " + clazz.clazz.name + "...")

				val generatedClass = generateClass(program, clazz)

				// Add dependencies for annotations
				// @TODO: Do this better!
				// @TODO: This should be recursive. But anyway it shouldn't be there!
				// @TODO: Analyzer should detect annotations and reference these ones
				generatedClass.classAndFieldAndMethodAnnotations.forEach {
					val classFq = it.type.name
					val clazz = tree.getSootClass(classFq)
					tree.addClassToGenerate(classFq.fqname)
					projectContext.addDep(AstClassRef(classFq))
					for (m in clazz.methods) {
						projectContext.addDep(m.astRef)
						projectContext.addDep(m.returnType.astType.getRefClasses())
						for (clazz in m.returnType.astType.getRefClasses()) {
							tree.addClassToGenerate(clazz.fqname)
						}
					}
				}
			}

			// Add synthetic methods to abstracts to simulate in haxe
			// @TODO: Maybe we could generate those methods in haxe generator
			for (clazz in program.classes.filter { it.isAbstract }) {
				for (method in clazz.getAllMethodsToImplement()) {
					if (!clazz.hasMethod(method)) {
						clazz.add(generateDummyMethod(clazz, method.name, method.type, false, AstVisibility.PUBLIC))
					}
				}
			}
		}

		//for (dep in projectContext.deps2!!) println(dep)

		println("Ok classes=${projectContext.classNames.size}, time=$elapsed")

		return program
	}

	fun generateMethod(containingClass: AstClass, method: SootMethod) = AstMethod(
		containingClass = containingClass,
		annotations = method.tags.toAstAnnotations(),
		name = method.name,
		type = method.astRef.type,
		body = if (method.isConcrete) AstMethodProcessor.processBody(method, containingClass) else null,
		signature = method.astType.mangle(),
		genericSignature = method.tags.filterIsInstance<SignatureTag>().firstOrNull()?.signature,
		defaultTag = method.tags.filterIsInstance<AnnotationDefaultTag>().firstOrNull()?.toAstAnnotation(),
		modifiers = method.modifiers,
		isStatic = method.isStatic,
		visibility = method.astVisibility,
		isNative = method.isNative
	)

	fun generateDummyMethod(containingClass: AstClass, name:String, methodType: AstType.METHOD_TYPE, isStatic: Boolean, visibility: AstVisibility) = AstMethod(
		containingClass = containingClass,
		annotations = listOf(),
		name = name,
		type = methodType,
		body = null,
		signature = methodType.mangle(),
		genericSignature = methodType.mangle(),
		defaultTag = null,
		modifiers = -1,
		isStatic = isStatic,
		visibility = visibility,
		isNative = true
	)

	fun generateField(containingClass: AstClass, field: SootField) = AstField(
		containingClass = containingClass,
		name = field.name,
		annotations = field.tags.toAstAnnotations(),
		type = field.type.astType,
		descriptor = field.type.astType.mangle(),
		genericSignature = field.tags.filterIsInstance<SignatureTag>().firstOrNull()?.signature,
		modifiers = field.modifiers,
		isStatic = field.isStatic,
		isFinal = field.isFinal,
		constantValue = field.tags.getConstant(),
		visibility = field.astVisibility
	)

	fun generateClass(program: AstProgram, sootClass: SootClass): AstClass {
		val context = program[BaseProjectContext.KEY]

		val astClass = AstClass(
			program = program,
			name = sootClass.name.fqname,
			modifiers = sootClass.modifiers,
			annotations = sootClass.tags.toAstAnnotations(),
			classType = if (sootClass.isInterface) AstClassType.INTERFACE else if (sootClass.isAbstract) AstClassType.ABSTRACT else AstClassType.CLASS,
			visibility = AstVisibility.PUBLIC,
			extending = if (sootClass.hasSuperclass() && !sootClass.isInterface) FqName(sootClass.superclass.name) else null,
			implementing = sootClass.interfaces.map { FqName(it.name) }
		)
		program.add(astClass)

		for (method in sootClass.methods.filter { context.mustInclude(it.astRef) }.map { generateMethod(astClass, it) }) {
			astClass.add(method)
		}

		//for (field in sootClass.fields.filter { context.mustInclude(it.astRef) }.map { generateField(astClass, it) }) {
		for (field in sootClass.fields.map { generateField(astClass, it) }) {
			astClass.add(field)
		}

		//astClass.finish()

		return astClass
	}
}


open class BaseProjectContext(val classNames: List<String>, val mainClass: String, val classPaths: List<String>, val output: SyncVfsFile, val deps2: HashSet<AstRef>?) {
	companion object {
		val KEY = UserKey<BaseProjectContext>()
	}

	class Tree {
		val generatedClasses = hashSetOf<FqName>()
		val classesToGenerate: Queue<FqName> = LinkedList<FqName>()

		fun addClassToGenerate(it: String) {
			if (it.fqname in generatedClasses) return
			classesToGenerate += it.fqname
			generatedClasses += it.fqname
		}

		fun getSootClass(fqname: FqName) = Scene.v().loadClassAndSupport(fqname.fqname)

		fun hasClassToGenerate() = classesToGenerate.isNotEmpty()

		fun getClassToGenerate() = classesToGenerate.remove()
	}

	val tree = Tree()
	val classes = arrayListOf<AstClass>()
	//val preInitLines = arrayListOf<String>()
	//val bootImports = arrayListOf<String>()

	fun mustInclude(ref: AstRef) = if (deps2 != null) (ref in deps2) else true
	fun isInterface(name: FqName) = tree.getSootClass(name).isInterface

	fun addDep(dep: AstRef) {
		this.deps2?.add(dep)
	}

	fun addDep(dep: Iterable<AstRef>) {
		this.deps2?.addAll(dep)
	}

}

open class AstMethodProcessor private constructor(
	private val method: SootMethod,
	private val containingClass: AstClass
) {
	private val program = containingClass.program
	private val context = program[BaseProjectContext.KEY]

	companion object {
		fun processBody(method: SootMethod, containingClass: AstClass): AstBody {
			return AstMethodProcessor(method, containingClass).handle()
		}
	}

	private val activeBody = method.retrieveActiveBody()
	private val units = activeBody.units.toList()
	private val traps = activeBody.traps.toList()

	private var labelIndex = 0
	private val labels = hashMapOf<soot.Unit, AstLabel>()

	private val locals = hashMapOf<String, AstLocal>()

	private fun ensureLabel(unit: soot.Unit): AstLabel {
		if (unit !in labels) {
			labelIndex++
			labels[unit] = AstLabel("label_$labelIndex")
		}
		return labels[unit]!!
	}

	private fun ensureLocal(c: soot.Local): AstLocal {
		if (locals[c.name] == null) {
			locals[c.name] = AstLocal(c.index, c.name, c.type.astType)
		}
		return locals[c.name]!!
	}

	private fun handle(): AstBody {
		prepareInternal(units)
		val stm = handleInternal(units)
		return AstBody(
			stm = stm,
			locals = locals.values.sortedBy { it.name },
			traps = traps.map { AstTrap(ensureLabel(it.beginUnit), ensureLabel(it.endUnit), ensureLabel(it.handlerUnit), it.exception.astType) }
		)
	}

	private fun prepareInternal(units: List<soot.Unit>) {
		for (trap in traps) {
			ensureLabel(trap.beginUnit)
			ensureLabel(trap.endUnit)
			ensureLabel(trap.handlerUnit)
		}
		for (unit in units) {
			when (unit) {
				is IfStmt -> ensureLabel(unit.target)
				is GotoStmt -> ensureLabel(unit.target)
				is LookupSwitchStmt -> {
					unit.targets.map { it as soot.Unit }.forEach { ensureLabel(it) }
					ensureLabel(unit.defaultTarget)
				}
				is TableSwitchStmt -> {
					unit.targets.map { it as soot.Unit }.forEach { ensureLabel(it) }
					ensureLabel(unit.defaultTarget)
				}
				else -> {
				}
			}
		}
	}

	private fun handleInternal(units: List<soot.Unit>): AstStm {
		var stms = arrayListOf<AstStm>()
		for (unit in units) {
			if (unit in labels) stms.add(AstStm.STM_LABEL(labels[unit]!!))
			stms.add(this.convert(unit))
		}
		return AstStm.STMS(stms.toList())
	}

	private fun convert(s: soot.Unit): AstStm = when (s) {
		is DefinitionStmt -> {
			val (l, r) = Pair(convert(s.leftOp), convert(s.rightOp))
			when (l) {
				is AstExpr.LOCAL -> AstStm.SET(l.local, r)
				is AstExpr.ARRAY_ACCESS -> AstStm.SET_ARRAY((l.array as AstExpr.LOCAL).local, l.index, r)
				is AstExpr.STATIC_FIELD_ACCESS -> AstStm.SET_FIELD_STATIC(l.clazzName, l.field, r, l.isInterface)
				is AstExpr.INSTANCE_FIELD_ACCESS -> AstStm.SET_FIELD_INSTANCE(l.expr, l.field, r)
				else -> invalidOp("Can't handle leftOp: $l")
			}
		}
		is ReturnStmt -> AstStm.RETURN(convert(s.op))
		is ReturnVoidStmt -> AstStm.RETURN(null)
		is IfStmt -> AstStm.IF_GOTO(convert(s.condition), ensureLabel(s.target))
		is GotoStmt -> AstStm.GOTO(ensureLabel(s.target))
		is ThrowStmt -> AstStm.THROW(convert(s.op))
		is InvokeStmt -> AstStm.STM_EXPR(convert(s.invokeExpr))
		is EnterMonitorStmt -> AstStm.MONITOR_ENTER(convert(s.op))
		is ExitMonitorStmt -> AstStm.MONITOR_EXIT(convert(s.op))
		is NopStmt -> AstStm.STMS(listOf())
		is LookupSwitchStmt -> AstStm.SWITCH_GOTO(
			convert(s.key), ensureLabel(s.defaultTarget),
			(0 until s.targetCount).map { Pair(s.getLookupValue(it), ensureLabel(s.getTarget(it))) }
		)
		is TableSwitchStmt -> AstStm.SWITCH_GOTO(
			convert(s.key), ensureLabel(s.defaultTarget),
			(s.lowIndex..s.highIndex).map { Pair(it, ensureLabel(s.getTarget(it - s.lowIndex))) }
		)
		else -> throw RuntimeException()
	}

	private fun simplify(expr: AstExpr): AstExpr {
		return if ((expr is AstExpr.CAST) && (expr.expr is AstExpr.LITERAL) && (expr.from == AstType.INT) && (expr.to == AstType.BOOL)) {
			AstExpr.LITERAL(expr.expr.value != 0)
		} else {
			// No simplified!
			expr
		}
	}

	private fun convert(c: Value): AstExpr = when (c) {
		is Local -> AstExpr.LOCAL(ensureLocal(c))
		is NullConstant -> AstExpr.LITERAL(null)
		is IntConstant -> AstExpr.LITERAL(c.value)
		is LongConstant -> AstExpr.LITERAL(c.value)
		is FloatConstant -> AstExpr.LITERAL(c.value)
		is DoubleConstant -> AstExpr.LITERAL(c.value)
		is StringConstant -> AstExpr.LITERAL(c.value)
		is ClassConstant -> AstExpr.CLASS_CONSTANT(AstType.REF_INT(c.value))
		is ThisRef -> AstExpr.THIS(FqName(method.declaringClass.name))
		is ParameterRef -> AstExpr.PARAM(AstArgument(c.index, c.type.astType))
		is CaughtExceptionRef -> AstExpr.CAUGHT_EXCEPTION(c.type.astType)
		is ArrayRef -> AstExpr.ARRAY_ACCESS(convert(c.base), convert(c.index))
		is InstanceFieldRef -> AstExpr.INSTANCE_FIELD_ACCESS(convert(c.base), c.field.ast, c.field.type.astType)
		is StaticFieldRef -> AstExpr.STATIC_FIELD_ACCESS(AstType.REF(c.field.declaringClass.name), c.field.ast, c.field.type.astType, c.field.declaringClass.isInterface)
		is CastExpr -> AstExpr.CAST(c.castType.astType, convert(c.op))
		is InstanceOfExpr -> AstExpr.INSTANCE_OF(convert(c.op), c.checkType.astType)
		is NewExpr -> AstExpr.NEW(c.type.astType as AstType.REF)
		is NewArrayExpr -> AstExpr.NEW_ARRAY(c.baseType.astType, listOf(convert(c.size)))
		is NewMultiArrayExpr -> AstExpr.NEW_ARRAY(c.baseType.astType, (0 until c.sizeCount).map { convert(c.getSize(it)) })
		is LengthExpr -> AstExpr.ARRAY_LENGTH(convert(c.op))
		is NegExpr -> AstExpr.UNOP(AstUnop.NEG, convert(c.op))
		is BinopExpr -> {
			// @TODO: Make this generic!
			val destType = c.type.astType
			val l = convert(c.op1)
			val r = convert(c.op2)
			val lType = l.type
			val rType = r.type
			val op = c.getAstOp(lType, rType)
			val r2 = if (c.op1.type is BooleanType && (op.symbol == "==") && (c.op2.type is IntType)) {
				simplify(AstExpr.CAST(AstType.BOOL, r))
			} else {
				r
			}
			AstExpr.BINOP(destType, l, op, r2)
		}
		is InvokeExpr -> {
			val argsList = c.args.toList()
			val castTypes = c.method.parameterTypes.map { it as Type }
			val args = Pair(argsList, castTypes).zipped.map {
				val (value, expectedType) = it
				doCastIfNeeded(expectedType, value)
			}.toList()
			val i = c
			when (i) {
				is StaticInvokeExpr -> {
					AstExpr.CALL_STATIC(AstType.REF(c.method.declaringClass.name), c.method.astRef, args)
				}
				is InstanceInvokeExpr -> {
					val isSpecial = i is SpecialInvokeExpr
					val obj = convert(i.base)
					val method = c.method.astRef
					val objType = obj.type
					var castToObject = false

					if (isSpecial && ((obj.type as AstType.REF).name != method.containingClass)) {
						AstExpr.CALL_SUPER(obj, method.containingClass, method, args, isSpecial)
					} else {
						if (objType is AstType.ARRAY) {
							castToObject = true
						} else if (objType is AstType.REF && context.isInterface(objType.name)) {
							castToObject = true
						}
						val obj2 = if (castToObject) AstExpr.CAST(method.classRef.type, obj) else obj
						AstExpr.CALL_INSTANCE(obj2, method, args, isSpecial)
					}
				}
				else -> throw RuntimeException()
			}
		}
		else -> throw RuntimeException()
	}

	final fun doCastIfNeeded(toType: Type, value: Value): AstExpr = if (value.type == toType) {
		convert(value)
	} else {
		AstExpr.CAST(value.type.astType, toType.astType, convert(value))
	}
}

fun BinopExpr.getAstOp(l: AstType, r: AstType): AstBinop {
	return when (this) {
		is AddExpr -> AstBinop.ADD
		is SubExpr -> AstBinop.SUB
		is MulExpr -> AstBinop.MUL
		is DivExpr -> AstBinop.DIV
		is RemExpr -> AstBinop.REM
		is AndExpr -> AstBinop.AND
		is OrExpr -> AstBinop.OR
		is XorExpr -> AstBinop.XOR
		is ShlExpr -> AstBinop.SHL
		is ShrExpr -> AstBinop.SHR
		is UshrExpr -> AstBinop.USHR
		is EqExpr -> AstBinop.EQ
		is NeExpr -> AstBinop.NE
		is GeExpr -> AstBinop.GE
		is LeExpr -> AstBinop.LE
		is LtExpr -> AstBinop.LT
		is GtExpr -> AstBinop.GT
		is CmpExpr -> if (l == AstType.LONG) AstBinop.LCMP else AstBinop.CMP
		is CmplExpr -> AstBinop.CMPL
		is CmpgExpr -> AstBinop.CMPG
		else -> throw RuntimeException()
	}
}

val SootClass.astRef: AstClassRef get() = AstClassRef(this.name)

val SootMethod.astRef: AstMethodRef get() = AstMethodRef(
	this.declaringClass.name.fqname, this.name,
	AstType.METHOD_TYPE(
		this.parameterTypes.withIndex().map { AstArgument(it.index, (it.value as Type).astType) },
		this.returnType.astType
	)
)
val SootField.astRef: AstFieldRef get() = AstFieldRef(this.declaringClass.name.fqname, this.name, this.type.astType, this.isStatic)

val SootMethod.astType: AstType.METHOD_TYPE get() = AstType.METHOD_TYPE(this.returnType.astType, this.parameterTypes.map { (it as Type).astType })

val SootClass.astType: AstType.REF get() = this.type.astType as AstType.REF

val Type.astType: AstType get() = when (this) {
	is VoidType -> AstType.VOID
	is BooleanType -> AstType.BOOL
	is ByteType -> AstType.BYTE
	is CharType -> AstType.CHAR
	is ShortType -> AstType.SHORT
	is IntType -> AstType.INT
	is FloatType -> AstType.FLOAT
	is DoubleType -> AstType.DOUBLE
	is LongType -> AstType.LONG
	is ArrayType -> AstType.ARRAY(baseType.astType, numDimensions)
	is RefType -> AstType.REF(FqName(className))
	is NullType -> AstType.NULL
	else -> throw NotImplementedError("toAstType: $this")
}

val SootField.ast: AstFieldRef get() = AstFieldRef(this.declaringClass.name.fqname, this.name, this.type.astType)

val FieldRef.ast: AstFieldRef get() = this.field.ast

val ClassMember.astVisibility: AstVisibility get() = if (this.isPublic) {
	AstVisibility.PUBLIC
} else if (this.isProtected) {
	AstVisibility.PROTECTED
} else {
	AstVisibility.PRIVATE
}

object SootUtils {
	fun init(classPaths: List<String>): Unit {
		G.reset()

		val Options_v = Options.v()

		//Options.v().set_output_format(Options.output_format_jimple)
		Options.v().set_output_format(Options.output_format_shimple)
		//Options.v().set_output_format(Options.output_format_grimple)
		Options_v.set_include_all(true)
		Options_v.set_print_tags_in_output(true)

		Options_v.set_allow_phantom_refs(false)
		//Options_v.set_allow_phantom_refs(true)

		Options_v.set_keep_line_number(true)

		val file_separator = OS.fileSeparator

		println("file_separator: $file_separator ... PathSeparator: ${File.pathSeparator}")

		Options_v.set_soot_classpath(classPaths.joinToString(File.pathSeparator))

		for (name in listOf("jap.npc", "jap.abc", "jop")) {
			Options_v.setPhaseOption(name, "enabled:true")
		}

		for (name in listOf(
			"jb.dae", "jb.uce", "jop.cse", "jop.bcm", "jop.lcm", "jop.cp",
			"jop.cpf", "jop.cbf", "jop.dae", "jop.nce", "jop.uce1", "jop.ubf1",
			"jop.uce2", "jop.ubf2", "jop.ule"
		)) {
			Options_v.setPhaseOption(name, "enabled:false")
		}

		Scene.v().loadNecessaryClasses()
	}

	// SootUtils.getTag(method.tags, "Llibcore/MethodBody;", "value") as String?
}

val ANNOTATIONS_BLACKLIST = listOf(
	"java.lang.annotation.Documented", "java.lang.Deprecated",
	"java.lang.annotation.Target", "java.lang.annotation.Retention",
	"kotlin.jvm.internal.KotlinLocalClass", "kotlin.jvm.internal.KotlinSyntheticClass",
	"kotlin.jvm.internal.KotlinClass", "kotlin.jvm.internal.KotlinFunction",
	"kotlin.jvm.internal.KotlinFileFacade", "kotlin.jvm.internal.KotlinMultifileClassPart",
	"kotlin.jvm.internal.KotlinMultifileClass", "kotlin.annotation.MustBeDocumented",
	"kotlin.annotation.Target", "kotlin.annotation.Retention",
	"kotlin.jvm.JvmStatic", "kotlin.Deprecated"
).map { AstType.REF(it) }.toSet()

fun Iterable<Tag>.toAstAnnotations(): List<AstAnnotation> {
	return this.filterIsInstance<VisibilityAnnotationTag>().flatMap {
		it.annotations.map {
			AstAnnotation(
				AstType.demangle(it.type) as AstType.REF,
				it.getElements().map {
					Pair(it.name, it.getValue())
				}.toMap()
			)
		}
	}.filter { it.type !in ANNOTATIONS_BLACKLIST }
}

fun AnnotationTag.getElements(): List<AnnotationElem> {
	return (0 until this.numElems).map { this.getElemAt(it) }
}

fun AnnotationElem?.getValue(): Any? = when (this) {
	null -> null
	is AnnotationStringElem -> this.value
	is AnnotationBooleanElem -> this.value
	is AnnotationIntElem -> this.value
	is AnnotationFloatElem -> this.value
	is AnnotationDoubleElem -> this.value
	is AnnotationLongElem -> this.value
	is AnnotationArrayElem -> this.values.map { it.getValue() }
	is AnnotationClassElem -> null
	is AnnotationEnumElem -> {
		val type = AstType.demangle(this.typeName) as AstType.REF
		AstFieldRef(type.name, this.constantName, type)
	}
	else -> {
		noImpl("Not implemented type: $this")
	}
}

fun AnnotationElem.unboxAnnotationElement() = Pair(this.name, this.getValue())

fun AnnotationDefaultTag.toAstAnnotation() = this.defaultVal.unboxAnnotationElement()

fun Iterable<Tag>?.getConstant(): Any? {
	val constantValue = this?.filterIsInstance<ConstantValueTag>()?.firstOrNull()
	return when (constantValue) {
		null -> null
		is IntegerConstantValueTag -> constantValue.intValue
		is LongConstantValueTag -> constantValue.longValue
		is DoubleConstantValueTag -> constantValue.doubleValue
		is FloatConstantValueTag -> constantValue.floatValue
		is StringConstantValueTag -> constantValue.stringValue
		else -> invalidOp("Not a valid constant")
	}
}
