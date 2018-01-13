import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstTypes
import com.jtransc.ast.dump
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.backend.asm1.AsmToAstMethodBody1
import com.jtransc.backend.asm2.AsmToAstMethodBody2
import com.jtransc.backend.hasBody
import com.jtransc.backend.isNative
import com.jtransc.backend.isStatic
import com.jtransc.gen.TargetName
import com.jtransc.io.readBytes
import com.jtransc.org.objectweb.asm.ClassReader
import com.jtransc.org.objectweb.asm.tree.ClassNode
import org.junit.Test

internal class AstExampleTest {
	private fun readClassNode(bytes: ByteArray): ClassNode {
		val cn = ClassNode()
		ClassReader(bytes).accept(cn, ClassReader.EXPAND_FRAMES)
		return cn
	}

	private fun readClassNode(clazz: Class<*>): ClassNode {
		return readClassNode(clazz.readBytes())
	}

	/*
	static public void main(String[] args) throws IOException {
		ClassNode clazz = readClassNode(AstExampleTest2.class);
		AstTypes types = new AstTypes();
		for (Object _method : clazz.methods) {
			MethodNode method = (MethodNode) _method;
			AstType.METHOD methodType = types.demangleMethod(method.desc);
			System.out.println("::" + method.name + " :: " + methodType);
			AstBody astBody = AsmToAstMethodBody1Kt.AsmToAstMethodBody1(
				types.REF_INT2(clazz.name),
				method,
				types,
				(clazz.sourceFile != null) ? clazz.sourceFile : (clazz.name + ".java")
			);
			System.out.println(Ast_dumpKt.dump(astBody));
			System.out.println(Ast_dumpKt.dump(new GotosFeature().remove(null, astBody, new AstBuildSettings(), types)));
			//System.out.println(Exp_dumpKt.dump(astBody));
		}
	}
	*/

	@Test
	fun name() {
		val clazz = readClassNode(AstExampleTest2::class.java)
		val types = AstTypes(TargetName("js"))
		for (_method in clazz.methods) {
			val methodType = types.demangleMethod(_method.desc)
			println("::" + _method.name + " :: " + methodType)
			if (_method.hasBody()) {
				val astBody = AsmToAstMethodBody1(
					types.REF_INT2(clazz.name),
					_method,
					types,
					if (clazz.sourceFile != null) clazz.sourceFile else clazz.name + ".java"
				)
				println(dump(types, astBody))
			}
			//println(dump(GotosFeature().remove(null, astBody, AstBuildSettings(), types)))
			//System.out.println(Exp_dumpKt.dump(astBody));
		}
	}

	@Test
	fun name2() {
		val clazz = readClassNode(AstExampleTest2::class.java)
		val types = AstTypes(TargetName("js"))
		for (_method in clazz.methods) {
			val methodType = types.demangleMethod(_method.desc)
			//if (_method.name == "<init>") continue
			println("::" + _method.name + " :: " + methodType)
			if (_method.hasBody()) {
				val astBody = AsmToAstMethodBody2(
					types.REF_INT2(clazz.name),
					_method,
					types,
					if (clazz.sourceFile != null) clazz.sourceFile else clazz.name + ".java"
				)
				println(dump(types, astBody))
			}
			//println(dump(GotosFeature().remove(null, astBody, AstBuildSettings(), types)))
			//System.out.println(Exp_dumpKt.dump(astBody));
		}
	}
}