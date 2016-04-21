import com.jtransc.ast.AstType;
import com.jtransc.io.ClassutilsKt;
import com.jtransc.types.Asm_astKt;
import com.jtransc.types.Exp_dumpKt;
import jtransc.io.JTranscIoTools;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
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
		//System.out.println(AstExampleTest.demo());

		ClassNode clazz = readClassNode(AstExampleTest2.class);
		//ClassNode clazz = readClassNode(JTranscIoTools.readFile(new File("/Users/soywiz/Projects/jtransc/jtransc/inception/example/Example.class")));
		for (Object _method : clazz.methods) {
			MethodNode method = (MethodNode) _method;
			AstType.METHOD methodType = AstType.Companion.demangleMethod(method.desc);
			System.out.println("::" + method.name + " :: " + methodType);
			//val jimple = Baf2Jimple(Asm2Baf(clazz, method))
			System.out.println(Exp_dumpKt.dump(Asm_astKt.Asm2Ast(AstType.Companion.REF_INT2(clazz.name), method)));
			//println(jimple)
		}
		//println(Asm2Baf(clazz, method).toExpr())
		//val builder = AstMethodBuilder(node.methods[0] as MethodNode)
	}


}
