package com.jtransc.backend

import com.jtransc.org.objectweb.asm.Opcodes
import com.jtransc.org.objectweb.asm.tree.AbstractInsnNode

fun AbstractInsnNode.isEndOfBasicBlock(): Boolean {
	return when (this.opcode) {
		in Opcodes.IFEQ..Opcodes.IF_ACMPNE -> true
		else -> isEnd()
	}
}

fun AbstractInsnNode.isEnd(): Boolean {
	return when (this.opcode) {
		in Opcodes.TABLESWITCH.. Opcodes.LOOKUPSWITCH -> true
		Opcodes.GOTO -> true
		Opcodes.ATHROW -> true
		in Opcodes.IRETURN..Opcodes.RETURN -> true
		else -> false
	}
}