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

import com.jtransc.ds.cast
import com.jtransc.error.InvalidOperationException
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.io.InputStream
import kotlin.properties.Delegates

object CFG {
	interface Node {

	}

	interface Edge {

	}
}

object MethodCFG {
	class BasicBlock(val first: AbstractInsnNode?, val id: Int) : CFG.Node {
		var last: AbstractInsnNode? = first
		val branches = arrayListOf<BasicBlock>()
		fun addBranch(ins: AbstractInsnNode?, other: BasicBlock) {
			branches.add(other)
		}

		fun dfs(): List<BasicBlock> {
			var out = arrayListOf<BasicBlock>()
			var visited = hashSetOf<BasicBlock>()
			fun visit(b: BasicBlock) {
				if (b in visited) return
				visited.add(b)
				out.add(b)
				b.branches.forEach { visit(it) }
			}
			return out.toList()
		}

		fun dump(visited: MutableSet<BasicBlock> = hashSetOf()) {
			if (this in visited) return
			visited.add(this)
			for (b in branches) {
				println("\"$this\" -> \"$b\"")
			}
			for (b in branches) {
				b.dump(visited)
			}
		}

		override fun toString() = "B$id"
	}

	class BasicBlockBranch : CFG.Edge {

	}

	class BasicBlockTree(
		val entry: BasicBlock
	) {
	}

	@JvmStatic fun main(args: Array<String>) {
		//println(Simple1::class.qualifiedName.replace())
		val path = Simple1::class.java.canonicalName.replace('.', '/')
		println(path)
		val clazz = readClass(javaClass.getResourceAsStream("/$path.class"))
		println(clazz.methods)
		for (method in clazz.getMethods()) {
			println("---- ${method.name}")
			if (method.name == "<init>") continue
			val cfg = createCFG(method)
			method.instructionList.forEach {
				println(it.toGenericString())
			}
			cfg.dump()
			//val result = method.analyze()
			//println("*******")
			//println(result.mainBlock.render())
		}
	}

	fun createCFG(m: MethodNode): BasicBlock {
		var id = 0
		val list = arrayListOf<AbstractInsnNode>()
		val endBlock = BasicBlock(null, -1)
		val blocks = hashMapOf<AbstractInsnNode, BasicBlock>()
		fun createBasicBlock(first: AbstractInsnNode): BasicBlock {
			if (first !in blocks) {
				val block = BasicBlock(first, id++)
				blocks[first] = block

				var node: AbstractInsnNode? = first

				// Search for jumps, switches, returns and throws
				loop@while (true) {
					if (node == null) {
						// Last instruction
						throw InvalidOperationException()
						//block.addBranch(node, endBlock)
						//break@loop
					}
					if (node !is LabelNode) {
						block.last = node
					}
					when (node) {
						is LabelNode -> {
							if (node != first) {
								block.addBranch(node, createBasicBlock(node))
								break@loop
							}
						}
						is JumpInsnNode -> {
							block.last = node
							if (node.opcode != Opcodes.GOTO) {
								block.addBranch(null, createBasicBlock(node.next))
							}
							block.addBranch(node, createBasicBlock(node.label))
							break@loop
						}
						is LookupSwitchInsnNode -> {
							for (label in node.labels.cast<LabelNode>()) {
								block.addBranch(node, createBasicBlock(label))
							}
							block.addBranch(node, createBasicBlock(node.dflt))
							break@loop
						}
						is TableSwitchInsnNode -> {
							for (label in node.labels.cast<LabelNode>()) {
								block.addBranch(node, createBasicBlock(label))
							}
							block.addBranch(node, createBasicBlock(node.dflt))
							break@loop
						}
						is InsnNode -> {
							when (node.opcode) {
								Opcodes.ATHROW, Opcodes.RETURN,
								Opcodes.ARETURN, Opcodes.IRETURN,
								Opcodes.FRETURN, Opcodes.LRETURN, Opcodes.DRETURN -> {
									block.addBranch(node, endBlock)
									break@loop
								}
							}
						}
					}
					node = node.next
				}
			}
			return blocks[first]!!
		}
		return createBasicBlock(m.instructions.first)
	}

	fun readClass(stream: InputStream): ClassNode {
		val classNode = ClassNode()
		ClassReader(stream).accept(classNode, ClassReader.EXPAND_FRAMES)
		return classNode
	}

	fun ClassNode.getMethods(): List<MethodNode> = this.methods.map { it as MethodNode }.toList()

	fun ClassNode.getMethods(name: String): List<MethodNode> = this.methods.map { it as MethodNode }.filter { it.name == name }.toList()

	fun ClassNode.getMethod(name: String): MethodNode = this.getMethods(name).first()

	val MethodNode.instructionList: List<AbstractInsnNode> get() {
		val out = arrayListOf<AbstractInsnNode>()
		for (i in this.instructions.iterator()) out.add(i as AbstractInsnNode)
		return out
	}
}