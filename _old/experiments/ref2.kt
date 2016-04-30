/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.input.asm

import com.jtransc.org.objectweb.asm.*

class ArrayReader<T>(val list: List<T>, var offset: Int) {

}

// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.6
class TestMethodVisitor : MethodVisitor(Opcodes.ASM5) {
	init {
		println("------------ METHOD")
	}

	override fun visitMultiANewArrayInsn(desc: String?, dims: Int) {
		println("visitMultiANewArrayInsn: $desc, $dims")
		super.visitMultiANewArrayInsn(desc, dims)
	}

	override fun visitFrame(type: Int, nLocal: Int, local: Array<out Any>?, nStack: Int, stack: Array<out Any>?) {
		println("visitFrame: $type, $nLocal, $local, $nStack, $stack")
		super.visitFrame(type, nLocal, local, nStack, stack)
	}

	override fun visitVarInsn(opcode: Int, `var`: Int) {
		println("visitVarInsn: $opcode, $`var`")
		super.visitVarInsn(opcode, `var`)
	}

	override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
		println("visitTryCatchBlock: $start, $end, $handler, $type")
		super.visitTryCatchBlock(start, end, handler, type)
	}

	override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
		println("visitLookupSwitchInsn: $dflt, $keys, $labels")
		super.visitLookupSwitchInsn(dflt, keys, labels)
	}

	override fun visitJumpInsn(opcode: Int, label: Label?) {
		println("visitJumpInsn: $opcode, $label")
		super.visitJumpInsn(opcode, label)
	}

	override fun visitLdcInsn(cst: Any?) {
		println("visitLdcInsn: $cst")
		super.visitLdcInsn(cst)
	}

	override fun visitIntInsn(opcode: Int, operand: Int) {
		println("visitIntInsn: $opcode, $operand")
		super.visitIntInsn(opcode, operand)
	}

	override fun visitTypeInsn(opcode: Int, type: String?) {
		println("visitTypeInsn: $opcode, $type")
		super.visitTypeInsn(opcode, type)
	}

	override fun visitAnnotationDefault(): AnnotationVisitor? {
		println("visitAnnotationDefault")
		return super.visitAnnotationDefault()
	}

	override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitAnnotation: $desc, $visible")
		return super.visitAnnotation(desc, visible)
	}

	override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitTypeAnnotation: $typeRef, $typePath, $desc, $visible")
		return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
	}

	override fun visitMaxs(maxStack: Int, maxLocals: Int) {
		println("visitMaxs: $maxStack, $maxLocals")
		super.visitMaxs(maxStack, maxLocals)
	}

	override fun visitInvokeDynamicInsn(name: String?, desc: String?, bsm: Handle?, vararg bsmArgs: Any?) {
		println("visitInvokeDynamicInsn: $name, $desc, $bsm, $bsmArgs")
		super.visitInvokeDynamicInsn(name, desc, bsm, *bsmArgs)
	}

	override fun visitLabel(label: Label?) {
		println("visitLabel: $label")
		super.visitLabel(label)
	}

	override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitTryCatchAnnotation: $typeRef, $typePath, $desc, $visible")
		return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible)
	}

	override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
		println("visitMethodInsn: $opcode, $owner, $name, $desc, $itf")
		super.visitMethodInsn(opcode, owner, name, desc, itf)
	}

	override fun visitInsn(opcode: Int) {
		println("visitInsn: $opcode")
		super.visitInsn(opcode)
	}

	override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitInsnAnnotation: $typeRef, $typePath, $desc, $visible")
		return super.visitInsnAnnotation(typeRef, typePath, desc, visible)
	}

	override fun visitParameterAnnotation(parameter: Int, desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitParameterAnnotation: $parameter, $desc, $visible")
		return super.visitParameterAnnotation(parameter, desc, visible)
	}

	override fun visitIincInsn(`var`: Int, increment: Int) {
		println("visitIincInsn: $`var`, $increment")
		super.visitIincInsn(`var`, increment)
	}

	override fun visitLineNumber(line: Int, start: Label?) {
		println("visitLineNumber: $line, $start")
		super.visitLineNumber(line, start)
	}

	override fun visitLocalVariableAnnotation(typeRef: Int, typePath: TypePath?, start: Array<out Label>?, end: Array<out Label>?, index: IntArray?, desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitLocalVariableAnnotation: $typeRef, $typePath, $start, $end, $index, $desc, $visible")
		return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible)
	}

	override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
		println("visitTableSwitchInsn: $min, $max, $dflt, $labels")
		super.visitTableSwitchInsn(min, max, dflt, *labels)
	}

	override fun visitEnd() {
		println("visitEnd")
		super.visitEnd()
	}

	override fun visitLocalVariable(name: String?, desc: String?, signature: String?, start: Label?, end: Label?, index: Int) {
		println("visitLocalVariable: $name, $desc, $signature, $start, $end, $index")
		super.visitLocalVariable(name, desc, signature, start, end, index)
	}

	override fun visitParameter(name: String?, access: Int) {
		println("visitParameter: $name, $access")
		super.visitParameter(name, access)
	}

	override fun visitAttribute(attr: Attribute?) {
		println("visitAttribute: $attr")
		super.visitAttribute(attr)
	}

	override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
		println("visitFieldInsn: $opcode, $owner, $name, $desc")
		super.visitFieldInsn(opcode, owner, name, desc)
	}

	override fun visitCode() {
		println("visitCode")
		super.visitCode()
	}
}

class TestFieldVisitor : FieldVisitor(Opcodes.ASM5) {
	init {
		println("------------ FIELD")
	}

	override fun visitEnd() {
		println("visitEnd")
		super.visitEnd()
	}

	override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitAnnotation: $desc, $visible")
		return super.visitAnnotation(desc, visible)
	}

	override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitTypeAnnotation: $typeRef, $typePath, $desc, $visible")
		return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
	}

	override fun visitAttribute(attr: Attribute?) {
		println("visitAttribute: $attr")
		super.visitAttribute(attr)
	}
}

class TestClassVisitor : ClassVisitor(Opcodes.ASM5) {
	init {
		println("------------ CLASS")
	}

	override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
		println("visitMethod: $access, $name, $desc, $signature, $exceptions")
		//return super.visitMethod(access, name, desc, signature, exceptions)
		return TestMethodVisitor()
	}

	override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
		println("visitInnerClass: $name, $outerName, $innerName, $access")
		super.visitInnerClass(name, outerName, innerName, access)
	}

	override fun visitSource(source: String?, debug: String?) {
		println("visitSource: $source, $debug")
		super.visitSource(source, debug)
	}

	override fun visitOuterClass(owner: String?, name: String?, desc: String?) {
		println("visitOuterClass: $owner, $name, $desc")
		super.visitOuterClass(owner, name, desc)
	}

	override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
		println("visit: $version, $access, $name, $signature, $superName, $interfaces")
		super.visit(version, access, name, signature, superName, interfaces)
	}

	override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor? {
		println("visitField: $access, $name, $desc, $signature, $value")
		return super.visitField(access, name, desc, signature, value)
	}

	override fun visitEnd() {
		println("visitEnd")
		super.visitEnd()
	}

	override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitAnnotation: $desc, $visible")
		return super.visitAnnotation(desc, visible)
	}

	override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor? {
		println("visitTypeAnnotation: $typeRef, $typePath, $desc, $visible")
		return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
	}

	override fun visitAttribute(attr: Attribute?) {
		println("visitAttribute: $attr")
		super.visitAttribute(attr)
	}
}

object Main4 {
	@JvmStatic fun main(args: Array<String>) {
		//println(Simple1::class.qualifiedName.replace())
		val path = Simple1::class.java.canonicalName.replace('.', '/')
		println(path)
		val clazzStream = javaClass.getResourceAsStream("/$path.class")
		ClassReader(clazzStream).accept(TestClassVisitor(), ClassReader.EXPAND_FRAMES)
	}
}
