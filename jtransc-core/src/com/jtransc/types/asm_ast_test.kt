package com.jtransc.types

import com.jtransc.ast.AstType
import com.jtransc.ast.demangleMethod
import com.jtransc.ds.cast
import com.jtransc.io.readBytes
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import java.io.File

class AstMethodBuilderTestExample(f: File?) {
	/*
	companion object {
		@JvmStatic fun add(a: Int, b: Int) = a + b
		@JvmStatic fun max(a: Int, b: Int) = if (a > b) a else b
		@JvmStatic fun max2(a: Int, b: Int) = (if (a > b) a * 2 else b * 3) * 4
		@JvmStatic fun max3(a: Long, b: Long) = (if (a > b) a * 2 else b * 3) * 4
		@JvmStatic fun callStatic() = add(1, 2) + add(3, 4)
		@JvmStatic fun callStatic2() {
			add(1, 2) + add(3, 4)
		}
		@JvmStatic fun sumAll(items: Array<Int>):Int {
			var sum = 0
			for (i in items) sum += i
			return sum
		}
		@JvmStatic fun sumAllPrim(items: IntArray):Int {
			var sum = 0
			for (i in items) sum += i
			return sum
		}
		@JvmStatic fun multiArray() = Array<Array<IntArray>>(0) { Array<IntArray>(0) { IntArray(0) } }
		@JvmStatic fun instantiate():Int {
			return MyClass().test() * 2;
		}
	}

	var a :Int = 10

	fun demo(b:Int) = (a + b).toLong()

	fun sample(str:String?) = if (str != null) File(str) else null
	*/

	constructor(str: String?) : this(if (str != null) File(str) else null)

	/*
	class MyClass() {
		fun test():Int {
			return 10;
		}
	}
	*/
}

object AstMethodBuilderTest {
	fun <T> Class<T>.readClassNode(): ClassNode {
		val bytes = this.readBytes()
		return ClassNode().apply {
			ClassReader(bytes).accept(this, ClassReader.EXPAND_FRAMES)
		}
	}

	@JvmStatic fun main(args: Array<String>) {
		val clazz = AstMethodBuilderTestExample::class.java.readClassNode()
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
