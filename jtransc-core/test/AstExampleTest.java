import com.jtransc.ast.*;
import com.jtransc.ast.feature.method.GotosFeature;
import com.jtransc.backend.asm1.AsmToAstMethodBody1Kt;
import com.jtransc.io.ClassutilsKt;
import com.jtransc.org.objectweb.asm.ClassReader;
import com.jtransc.org.objectweb.asm.tree.ClassNode;
import com.jtransc.org.objectweb.asm.tree.MethodNode;

import java.io.IOException;

class AstExampleTest {
	static private ClassNode readClassNode(byte[] bytes) {
		ClassNode cn = new ClassNode();
		new ClassReader(bytes).accept(cn, ClassReader.EXPAND_FRAMES);
		return cn;
	}

	static private ClassNode readClassNode(Class<?> clazz) {
		return readClassNode(ClassutilsKt.readBytes(clazz));
	}

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
}