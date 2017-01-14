package com.jtransc

import com.jtransc.ds.diff
import com.jtransc.ds.hasAnyFlags
import com.jtransc.ds.hasFlag
import com.jtransc.backend.toAst
import com.jtransc.maven.MavenLocalRepository
import com.jtransc.vfs.GetClassJar
import com.jtransc.vfs.MergedLocalAndJars
import com.jtransc.vfs.SyncVfsFile
import com.jtransc.vfs.ZipVfs
import com.jtransc.JTranscVersion
import com.jtransc.annotation.haxe.HaxeMethodBody
import com.jtransc.ast.*
import com.jtransc.org.objectweb.asm.ClassReader
import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.AnnotationNode
import com.jtransc.org.objectweb.asm.tree.ClassNode
import com.jtransc.org.objectweb.asm.tree.FieldNode
import com.jtransc.org.objectweb.asm.tree.MethodNode

class JTranscRtReport {
	val types = AstTypes()

	companion object {
		@JvmStatic fun main(args: Array<String>) {
			JTranscRtReport().report()
		}
	}

	val jtranscVersion = JTranscVersion.getVersion()
	val javaRt = ZipVfs(GetClassJar(String::class.java))
	val jtranscRt = MergedLocalAndJars(MavenLocalRepository.locateJars("com.jtransc:jtransc-rt:$jtranscVersion"))

	fun report() {
		reportPackage("java", listOf("java.rmi", "java.sql", "java.beans", "java.awt", "java.applet"))
		reportNotImplementedNatives("java")
	}

	fun reportPackage(packageName: String, ignoreSet: List<String>) {
		val ignoreSetNormalized = ignoreSet.map { it.replace('.', '/') }
		val packagePath = packageName.replace('.', '/')
		fileList@for (e in javaRt[packagePath].listdirRecursive().filter { it.name.endsWith(".class") }) {
			//for (e.file)
			for (base in ignoreSetNormalized) {
				if (e.file.path.startsWith(base)) {
					continue@fileList
				}
			}
			compareFiles(e.file, jtranscRt[e.path])
		}
	}

	fun reportNotImplementedNatives(packageName: String) {
		val packagePath = packageName.replace('.', '/')
		fileList@for (e in jtranscRt[packagePath].listdirRecursive().filter { it.name.endsWith(".class") }) {
			val clazz = readClass(e.file.readBytes())
			val nativeMethodsWithoutBody = clazz.methods.filterIsInstance<MethodNode>()
				.filter { it.access hasFlag Opcodes.ACC_NATIVE }

			if (nativeMethodsWithoutBody.isNotEmpty()) {
				println("${clazz.name} (native without body):")
				for (method in nativeMethodsWithoutBody.filter { method ->
					if (method.invisibleAnnotations != null) {
						!AstAnnotationList(
							AstMethodRef(clazz.name.fqname, method.name, types.demangleMethod(method.desc)),
							method.invisibleAnnotations.filterIsInstance<AnnotationNode>().map { it.toAst(types) }).contains<HaxeMethodBody>()
					} else {
						true
					}
				}) {
					println(" - ${method.name} : ${method.desc}")
				}
			}
		}
	}

	private fun readClass(data: ByteArray): ClassNode {
		return ClassNode(Opcodes.ASM5).apply {
			ClassReader(data).accept(this, ClassReader.SKIP_CODE + ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES)
		}
	}

	fun compareFiles(f1: SyncVfsFile, f2: SyncVfsFile) {
		//interface MemberRef {}
		data class MethodRef(val name: String, val desc: String)

		fun ClassNode.getPublicOrProtectedMethodDescs() = this.methods
			.filterIsInstance<MethodNode>()
			.filter { it.access hasAnyFlags (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) }
			.map { MethodRef(it.name, it.desc) }

		fun ClassNode.getPublicOrProtectedFieldDescs() = this.methods
			.filterIsInstance<FieldNode>()
			.filter { it.access hasAnyFlags (Opcodes.ACC_PUBLIC or Opcodes.ACC_PROTECTED) }
			.map { AstFieldWithoutClassRef(it.name, types.demangle(it.desc)) }

		if (f1.exists) {
			val javaClass = readClass(f1.readBytes())
			if (javaClass.access hasAnyFlags Opcodes.ACC_PUBLIC) {
				if (f2.exists) {
					val jtranscClass = readClass(f2.readBytes())

					val javaMethods = javaClass.getPublicOrProtectedMethodDescs()
					val jtranscMethods = jtranscClass.getPublicOrProtectedMethodDescs()

					val javaFields = javaClass.getPublicOrProtectedFieldDescs()
					val jtranscFields = jtranscClass.getPublicOrProtectedFieldDescs()

					val methodResults = javaMethods.diff(jtranscMethods)
					val fieldResults = javaFields.diff(jtranscFields)

					if (methodResults.justFirst.isNotEmpty()) {
						println("${javaClass.name} (missing methods)")
						for (i in methodResults.justFirst) {
							println(" - ${i.name} : ${i.desc}")
						}
					}

					if (fieldResults.justFirst.isNotEmpty()) {
						println("${javaClass.name} (missing fields)")
						for (i in fieldResults.justFirst) {
							println(" - ${i.name} : ${i.type.mangle()}")
						}
					}
				} else {
					println("${javaClass.name} (missing class)")
				}
			}
			//for (node in origClass.methods.filterIsInstance<MethodNode>()) node.name + node.desc
			//println("" + f1.path + " : " + f2.path)
		}
	}
}