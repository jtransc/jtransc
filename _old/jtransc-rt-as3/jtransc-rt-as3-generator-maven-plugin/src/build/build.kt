package build

import com.jtransc.ast.FqName
import com.jtransc.lang.getResourceAsString
import com.jtransc.text.GenericTokenize
import com.jtransc.text.Indenter
import com.jtransc.text.TokenReader
import com.jtransc.text.toUcFirst
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MemoryVfs
import com.jtransc.vfs.SyncVfsFile
import org.apache.tools.ant.Project
import org.apache.tools.ant.ProjectHelper
import java.io.File
import java.io.StringReader

fun main(args: Array<String>) {
	//generateAirCoreJar("aircore-18.jar")
	val resource = As3Parser::class.java.getResourceAsString("/airglobal.as.txt")
	print("Tokenizing...")
	val tokens = GenericTokenize(StringReader(resource))
	println("Ok")
	print("Parsing...")
	val tr = TokenReader(tokens)
	val traits = As3Parser(tr).readTraits()
	println("Ok")
	print("Writting traits...")
	val vfs = LocalVfs("")["jtransc-rt-as3/jtransc-rt-as3/src"]
	writeTraits(traits, vfs.ensuredir())
	println("Ok")
	print("Building jar...")
	runant(vfs)
	println("Ok")
}

fun generateAirCoreSource(airCorePath: String): Unit {
	val resource = As3Parser::class.java.getResourceAsString("/airglobal.as.txt")
	print("Tokenizing...")
	val tokens = GenericTokenize(StringReader(resource))
	println("Ok")
	print("Parsing...")
	val tr = TokenReader(tokens)
	val traits = As3Parser(tr).readTraits()
	println("Ok")
	print("Writting traits...")
	File(airCorePath).mkdirs()
	val vfs = LocalVfs(airCorePath)
	writeTraits(traits, vfs.ensuredir())
	println("Ok")
}

fun writeTraits(traits: List<As3Parser.Trait>, vfs: SyncVfsFile = MemoryVfs()): SyncVfsFile {
	fun processTrait(trait: As3Parser.Trait, parentTrait: As3Parser.Trait? = null, allowNative: Boolean = true, forceStatic: Boolean = false, forcePackagePath: String = ""): Indenter = Indenter.gen {
		fun fixname(name: String) = when (name) {
			"flash.root.Function" -> "all.core.AllFunction"
			else -> name
		}

		val visibility = trait.visibility.str
		val retval = fixname(trait.retval.str)
		val isConstructor = trait.isConstructor
		val packagePath = trait.name.packagePath
		val isStatic = trait.isStatic || forceStatic
		val static = if (isStatic) "static " else ""
		val traitType = trait.type.str
		val isInterface = trait.type == TraitType.INTERFACE
		var native = if (allowNative && !isConstructor) "native " else ""
		val simplename = trait.encodedSimpleName
		val originalName = trait.originalSimpleName

		if (trait.visibility == VISIBILITY.PUBLIC || trait.visibility == VISIBILITY.PROTECTED) {
			when (trait.type) {
				TraitType.CLASS, TraitType.INTERFACE -> {
					var implementsList = trait.implementsList

					if (originalName == "Function") {
						implementsList = listOf(As3Parser.Type.REF(FqName("all.core.AllFunction"))) + implementsList
					}

					val extends = if (trait.extensionList.isNotEmpty()) {
						" extends " + trait.extensionList.map { it.str }.joinToString(", ")
					} else ""
					val implements = if (implementsList.isNotEmpty()) {
						" implements " + implementsList.map { it.str }.joinToString(", ")
					} else ""

					line("package $packagePath;")
					line("@all.annotation.AllNativeClass(\"${trait.originalName}\")")
					line("public $traitType $simplename$extends$implements") {
						val constructor = trait.children.firstOrNull { it.isConstructor }
						for (ctrait in trait.children.distinctBy { it.simplename }) {
							line(processTrait(ctrait, trait, allowNative = !isInterface))
						}
					}
				}
				TraitType.FUNCTION -> {
					val optionalArgumentsOffset = trait.arguments.indexOfFirst { it.defaultValue != null }
					val optionalArgumentsOffsetReal = if (optionalArgumentsOffset < 0) trait.arguments.size else optionalArgumentsOffset
					val argumentsFull = trait.arguments.take(optionalArgumentsOffsetReal)
					val argumentsOptional = trait.arguments.drop(optionalArgumentsOffsetReal)
					var argsCombinations = (0..argumentsOptional.size).map { argumentsFull + argumentsOptional.take(it) }

					var skipMethod = false

					if (simplename == "toString") skipMethod = true
					if (parentTrait?.simplename == "__HTMLScriptArray" && simplename in setOf("join", "every", "some")) skipMethod = true
					if (isConstructor && argsCombinations.first().isNotEmpty()) {
						argsCombinations = listOf(listOf<As3Parser.Argument>()) + argsCombinations
					}

					for (argsCombination in argsCombinations) {
						// Incompatible with java
						if (simplename == "wait" && argsCombination.isEmpty() && !isStatic) continue

						val args = argsCombination.map { "${fixname(it.type.str)} ${it.name}" }.joinToString(", ")
						if (skipMethod) {
							// Avoid method
						} else if (isConstructor) {
							line("$visibility $simplename($args) { super(); } ")
						} else {
							val methodName = "$forcePackagePath.$originalName".trim('.')
							line("@all.annotation.AllMethod(\"$methodName\") $static $native $visibility $retval $simplename($args);")
						}
					}
				}
				TraitType.SETTER -> {
					val arg = fixname(trait.arguments.first().type.str)
					val methodName = "$forcePackagePath.$originalName".trim('.')
					line("@all.annotation.AllSetter(\"$methodName\") $static $visibility $native void $simplename($arg value);")
				}
				TraitType.GETTER -> {
					val methodName = "$forcePackagePath.$originalName".trim('.')
					if (retval.endsWith("[]")) {
						line("@all.annotation.AllGetter(\"(all.native.As3Natives.toArray((\$).$methodName))\") $static $visibility $native $retval $simplename();")
					} else {
						line("@all.annotation.AllGetter(\"$methodName\") $static $visibility $native $retval $simplename();")
					}
				}
				TraitType.VAR -> {
					line("$static $visibility $retval $simplename;")
				}
				TraitType.CONST -> {
					if (trait.defaultValue != null) {
						val _value = "${trait.defaultValue}"
						val value = when (_value) {
							"-Infinity" -> "java.lang.Double.NEGATIVE_INFINITY"
							"Infinity" -> "java.lang.Double.POSITIVE_INFINITY"
							"NaN" -> "java.lang.Double.NaN"
							else -> _value
						}
						val value2 = if (retval == "String" && !value.startsWith('"')) "\"$value\""
						else if (simplename == "DATA_EVENT_TIMEOUT") "1800000"
						else if (simplename == "MAX_VALUE" && parentTrait?.encodedSimpleName == "uint") "99"
						else "$value"

						line("$static $visibility $retval $simplename = $value2;")
					} else {
						line("$static $visibility $retval $simplename;")
					}
				}
				else -> {
				}
			}
		}
	}


	for (trait in traits.filter { it.type.isClassType() }) {
		val classFile = trait.name.packagePath.replace('.', '/') + "/" + trait.encodedSimpleName + ".java"
		//println(trait)
		vfs[classFile] = processTrait(trait).toString()
	}

	for (traitPackage in traits.filter { !it.type.isClassType() }.groupBy { it.name.packagePath }) {
		val packagePath = traitPackage.key
		val traits2 = traitPackage.value
		val className = packagePath.substringAfterLast('.').toUcFirst()
		val classFile = packagePath.replace('.', '/') + "/$className.java"
		vfs[classFile] = Indenter.gen {
			line("package $packagePath;")
			line("@all.annotation.AllPackageClass(\"$packagePath\") @all.annotation.AllNativeClass(\"$packagePath\") public class $className") {
				for (trait in traits2) {
					//println(trait)
					line(processTrait(trait, forceStatic = true, forcePackagePath = ""))
				}
			}
		}.toString()
	}

	vfs["all/annotation/AllGetter.java"] = """
		package all.annotation;
		@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
		@java.lang.annotation.Target(value={java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD})
		public @interface AllGetter { String value(); }
	"""

	vfs["all/annotation/AllSetter.java"] = """
		package all.annotation;
		@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
		@java.lang.annotation.Target(value={java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD})
		public @interface AllSetter { String value(); }
	"""

	vfs["all/annotation/AllMethod.java"] = """
		package all.annotation;
		@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
		@java.lang.annotation.Target(value={java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.METHOD})
		public @interface AllMethod { String value(); }
	"""

	vfs["all/annotation/AllNativeClass.java"] = """
		package all.annotation;
		@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
		@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE})
		public @interface AllNativeClass { String value(); }
	"""

	vfs["all/annotation/AllPackageClass.java"] = """
		package all.annotation;
		@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)
		@java.lang.annotation.Target(value={java.lang.annotation.ElementType.TYPE})
		public @interface AllPackageClass { String value(); }
	"""

	vfs["all/as3/As3Lib.java"] = """
		package all.as3;
		import flash.display.Stage;
		import flash.display.Sprite;
		public class As3Lib {
			@all.annotation.AllKeep static public Stage stage;
			@all.annotation.AllKeep static public Sprite root;
		}
	"""

	vfs["all/core/AllFunction.java"] = """package all.core; public interface AllFunction { }"""
	vfs["all/core/AllFunctionDyn.java"] = """package all.core; public interface AllFunctionDyn<TR> extends AllFunction { @all.annotation.AllKeep TR execute(); }"""
	vfs["all/core/AllFunction0.java"] = """package all.core; public interface AllFunction0<TR> extends AllFunction { @all.annotation.AllKeep TR execute(); }"""
	vfs["all/core/AllFunction1.java"] = """package all.core; public interface AllFunction1<TR, T1> extends AllFunction { @all.annotation.AllKeep TR execute(T1 arg1); }"""
	vfs["all/core/AllFunction2.java"] = """package all.core; public interface AllFunction2<TR, T1, T2> extends AllFunction { @all.annotation.AllKeep TR execute(T1 arg1, T2 arg2); }"""
	vfs["all/core/AllProcedure0.java"] = """package all.core; public interface AllProcedure0 extends AllFunction { @all.annotation.AllKeep void execute(); }"""
	vfs["all/core/AllProcedure1.java"] = """package all.core; public interface AllProcedure1<T1> extends AllFunction { @all.annotation.AllKeep void execute(T1 arg1); }"""
	vfs["all/core/AllProcedure2.java"] = """package all.core; public interface AllProcedure2<T1, T2> extends AllFunction { @all.annotation.AllKeep void execute(T1 arg1, T2 arg2); }"""

	return vfs
}

fun runant(vfs: SyncVfsFile) {
	val buildFile = File("${vfs.realpath}/../build.xml")
	buildFile.writeText("""<project name="BuildAs3Stubs" basedir="." default="main">

    <target name="clean">
        <delete dir="build"/>
    </target>

    <target name="compile">
        <mkdir dir="build/classes"/>
        <javac
            srcdir="src"
            destdir="build/classes"
            debug="on"
            debuglevel="lines,vars,source"
         />
    </target>

    <target name="jar">
        <mkdir dir="build/jar"/>
        <jar destfile="../../libs/aircore-18.jar" basedir="build/classes">
            <manifest>
            <!--
                <attribute name="Main-Class" value="oata.HelloWorld"/>
                -->
            </manifest>
        </jar>
    </target>

    <target name="run">
        <java jar="build/jar/HelloWorld.jar" fork="true"/>
    </target>

    <target name="main" depends="compile,jar">
    </target>

</project>""")
	val p = Project()
	//println(System.getenv("JAVA_HOME"))
	p.setUserProperty("ant.file", buildFile.absolutePath)
	p.setUserProperty("JAVA_HOME", System.getenv("JAVA_HOME"))
	p.init()
	val helper = ProjectHelper.getProjectHelper()
	p.addReference("ant.projectHelper", helper)
	helper.parse(p, buildFile)
	p.executeTarget(p.defaultTarget)
}

enum class VISIBILITY(val str: String) { AS3("public"), FLASH_PROXY("public"), PUBLIC("public"), PRIVATE("private"), PROTECTED("protected"), INTERNAL("internal") }
enum class TraitType(val str: String) {
	CLASS("class"), INTERFACE("interface"), FUNCTION("function"), SETTER("setter"), GETTER("getter"), VAR("var"), CONST("const"), UNKNOWN("<unknown>");

	fun isClassType() = this == TraitType.CLASS || this == TraitType.INTERFACE
	fun isFunctionType() = this == TraitType.FUNCTION
	fun isVarLike() = this == TraitType.GETTER || this == TraitType.SETTER || this == TraitType.VAR || this == TraitType.CONST
}

class As3Parser(val r: TokenReader<String>) {

	interface Type {
		val str: String

		object VOID : Type {
			override val str: String = "void"
		}

		object INT : Type {
			override val str: String = "int"
		}

		object DOUBLE : Type {
			override val str: String = "double"
		}

		object BOOLEAN : Type {
			override val str: String = "boolean"
		}

		object VARARG : Type {
			override val str: String = "Object..."
		}

		data class VECTOR(val item: Type) : Type {
			override val str: String = "${item.str}[]"
		}

		data class REF(val fqname: FqName) : Type {
			override val str: String = fqname.fqname
		}
	}

	data class Argument(val name: String, val type: Type, val defaultValue: Any? = null)

	data class Trait(
		val name: FqName,
		val originalName: String,
		val type: TraitType,
		val isStatic: Boolean,
		val visibility: VISIBILITY,
		val extensionList: List<Type>,
		val implementsList: List<Type>,
		val arguments: List<Argument>,
		val retval: Type,
		val isConstructor: Boolean,
		val defaultValue: Any?,
		val children: List<Trait> = listOf()
	) {
		val originalSimpleName = name.simpleName
		val simplename = when (type) {
			TraitType.GETTER -> "get${name.simpleName.toUcFirst()}"
			TraitType.SETTER -> "set${name.simpleName.toUcFirst()}"
			else -> name.simpleName
		}
		val encodedSimpleName = when (simplename) {
			"int" -> "Int"
			"notify" -> "_notify"
			"notifyAll" -> "_notifyAll"
			else -> simplename
		}
	}

	fun parseType(): Type {
		val readed = parseFqId()
		if (readed == "Vector.<") {
			val type = parseType()
			r.expect(">")
			return Type.VECTOR(type)
		}
		return when (readed) {
			"void" -> Type.VOID
			"int", "uint" -> Type.INT
			"Number" -> Type.DOUBLE
			"Boolean" -> Type.BOOLEAN
			"*" -> Type.REF(FqName("java.lang.Object"))
			"Object" -> Type.REF(FqName("flash.root.Object"))
			"Class" -> Type.REF(FqName("flash.root.Class"))
			"Array" -> Type.REF(FqName("flash.root.Array"))
			"XML" -> Type.REF(FqName("flash.root.XML"))
			"JSON" -> Type.REF(FqName("flash.root.JSON"))
			"Function" -> Type.REF(FqName("flash.root.Function"))
			"Error" -> Type.REF(FqName("flash.root.Error"))
			"QName" -> Type.REF(FqName("flash.root.QName"))
			"Date" -> Type.REF(FqName("flash.root.Date"))
			"XMLList" -> Type.REF(FqName("flash.root.XMLList"))

			else -> Type.REF(FqName(readed))
		}
	}

	fun parseTypeTag(): Type {
		r.expect(":")
		return parseType()
	}

	fun parseFqId(): String {
		var chunks = arrayListOf<String>()
		while (r.hasMore) {
			val res = r.read()
			chunks.add(res)
			if (!r.tryRead(".")) break
		}
		return chunks.joinToString(".")
	}

	fun parseExpr(): String {
		val readed = r.read()
		if (readed == "-") {
			val readed2 = r.read()
			return "-$readed2"
			//println("EXPR: $readed2")
		} else {
			return readed
			//println("EXPR: $readed")
		}
	}

	fun parseOptValue(): Any? {
		if (r.tryRead("=")) {
			return parseExpr()
		}
		return null
	}

	fun parseArgument(): Argument {
		val name = r.read()
		if (name == "...") {
			val realName = r.read()
			//println("PARAM with ...: $realName")
			return Argument(realName, Type.VARARG, null)
		} else {
			//println("PARAM: $name")
			val type = parseTypeTag()
			val value = parseOptValue()
			return Argument(name, type, value)
		}
	}

	fun parseTrait(parentClassName: String = "--"): Trait {
		var name = ""
		var isNative = false
		var isDynamic = false
		var isStatic = false
		var isFinal = false
		var isOverriding = false
		var visibility = VISIBILITY.PUBLIC
		var type = TraitType.UNKNOWN
		val traits = arrayListOf<Trait>()
		var extensionList = arrayListOf<Type>()
		var implementsList = arrayListOf<Type>()
		var arguments = arrayListOf<Argument>()
		var retval: Type = Type.VOID
		var defaultValue: Any? = null
		var isConstructor = false

		loop@while (true) {
			val tok = r.read()
			when (tok) {
				"native" -> isNative = true
				"static" -> isStatic = true
				"final" -> isFinal = true
				"override" -> isOverriding = true
				"AS3" -> visibility = VISIBILITY.PUBLIC
				"flash_proxy" -> visibility = VISIBILITY.FLASH_PROXY
				"__AS3__\$vec" -> visibility = VISIBILITY.AS3
				"public" -> visibility = VISIBILITY.PUBLIC
				"private" -> visibility = VISIBILITY.PRIVATE
				"protected" -> visibility = VISIBILITY.PROTECTED
				"internal" -> visibility = VISIBILITY.PROTECTED
				"dynamic" -> isDynamic = true
				"class", "interface" -> {
					type = if (tok == "class") TraitType.CLASS else TraitType.INTERFACE
					name = parseFqId()
					//println("class:$name")
					if (r.tryRead("extends")) {
						do {
							extensionList.add(parseType())
							if (r.tryRead(",")) {
								continue
							} else {
								break
							}
						} while (true)
					}
					if (r.tryRead("implements")) {
						do {
							implementsList.add(parseType())
							if (r.tryRead(",")) {
								continue
							} else {
								break
							}
						} while (true)
					}
					r.expect("{")
					while (r.peek() != "}") {
						val trait = parseTrait(name)
						//println(trait)
						traits.add(trait)
					}
					r.expect("}")
					break@loop
				}
				"function" -> {
					type = TraitType.FUNCTION
					var tryname = r.peek()
					if (r.tryRead("get", "set")) {
						if (r.peek() == "(") {
							name = tryname
						} else {
							type = if (tryname == "get") TraitType.GETTER else TraitType.SETTER

							name = parseFqId()
						}
					} else {
						name = parseFqId()
					}
					//println("TRAIT:$name")
					r.expect("(")
					//println(name)
					while (r.peek() != ")") {
						val param = parseArgument()
						arguments.add(param)
						r.tryRead(",")
					}
					r.expect(")")
					retval = parseTypeTag()
					r.expect(";")
					val parentSimpleClassName = parentClassName.split('.').last()
					isConstructor = name == parentSimpleClassName
					break@loop
				}
				"const", "var" -> {
					type = if (tok == "const") TraitType.CONST else TraitType.VAR
					name = parseFqId()
					retval = parseTypeTag()
					defaultValue = parseOptValue()
					r.expect(";")
					break@loop
				}
				"[" -> {
					val annotationName = r.read()
					if (r.tryRead("(")) {
						while (r.peek() != ")") {
							val paramName = r.read()
							if (r.tryRead(",")) {

							} else if (r.tryRead("=")) {
								r.read()
								if (r.expect(setOf(",", ")")) == ")") {
									r.unread()
									break
								}
							}
						}
						r.expect(")")
					}
					r.expect("]")
					//println("annotation!")
				}
				else -> {
					println("Unexpected/extra: $tok")
					//r.unread()
					//break@loop
				}
			}
		}

		if (extensionList.size == 1 && extensionList[0].str == "Object") extensionList.clear()

		return Trait(
			name = if (FqName(name).packagePath.isEmpty()) FqName("flash.root.$name") else FqName(name),
			originalName = name,
			type = type,
			isStatic = isStatic,
			visibility = visibility,
			extensionList = extensionList,
			implementsList = implementsList,
			arguments = arguments,
			retval = retval,
			isConstructor = isConstructor,
			defaultValue = defaultValue,
			children = traits.toList()
		)
	}

	fun readTraits(): List<Trait> {
		var out = arrayListOf<Trait>()
		while (r.hasMore) {
			val start = r.position
			out.add(parseTrait())
			val end = r.position
			if (end == start) {
				println("Not parted all!")
				break
			}
		}
		return out.toList()
	}
}
