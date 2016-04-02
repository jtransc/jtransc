package com.jtransc.types

import com.jtransc.ast.AstType
import com.jtransc.ast.demangleMethod
import com.jtransc.ds.cast
import com.jtransc.io.readBytes
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.File
import java.io.IOException
import java.lang.Throwable

object AstMethodBuilderTest {
	fun <T> Class<T>.readClassNode(): ClassNode {
		val bytes = this.readBytes()
		return ClassNode().apply {
			ClassReader(bytes).accept(this, ClassReader.EXPAND_FRAMES)
		}
	}

	@JvmStatic fun main(args: Array<String>) {
		//System.out.println(AstTestExample.demo());
		val clazz = AstTestExample::class.java.readClassNode()
		for (method in clazz.methods.cast<MethodNode>()) {
			val methodType = AstType.demangleMethod(method.desc)
			println("::${method.name} :: $methodType")
			//val jimple = Baf2Jimple(Asm2Baf(clazz, method))
			println(dump(Asm2Ast(AstType.REF_INT2(clazz.name), method)))
			//println(jimple)
		}
		//println(Asm2Baf(clazz, method).toExpr())
		//val builder = AstMethodBuilder(node.methods[0] as MethodNode)
	}
}
