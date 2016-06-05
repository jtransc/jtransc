package com.jtransc.vfs.node

import com.jtransc.numeric.toInt
import com.jtransc.vfs.FileMode
import com.jtransc.vfs.UserData
import java.io.File
import java.util.*

open class FileNodeTree {
	val root = FileNode(this, null, "", FileNodeType.ROOT)
}

enum class FileNodeType { ROOT, DIRECTORY, FILE }

interface FileNodeIO {
	fun read(): ByteArray
	fun write(data: ByteArray): Unit
	fun size(): Long
	fun mtime(): Date
	fun mode(): FileMode
}

open class FileNode(val tree: FileNodeTree, val parent: FileNode?, val name: String, var type: FileNodeType) : Iterable<FileNode> {
	private val children = arrayListOf<FileNode>()
	val userData = UserData()
	var io: FileNodeIO? = null

	override fun toString(): String = "FileNode($path, type=$type leaf=$isLeaf)"

	val path: String get() = if (parent != null) "${parent.path}/$name" else name
	val root: FileNode get() = if (parent != null) parent.root else this
	val isLeaf: Boolean get() = children.size == 0

	init {
		if (parent != null) parent.children.add(this)
	}

	fun size(): Long = io?.size() ?: 0
	fun mtime(): Date = io?.mtime() ?: Date()
	fun mode(): FileMode = io?.mode() ?: FileMode.FULL_ACCESS
	fun isDirectory(): Boolean = (type == FileNodeType.ROOT) || (type == FileNodeType.DIRECTORY)

	fun getChildren(): List<FileNode> = children

	override fun iterator(): Iterator<FileNode> {
		return children.iterator()
	}

	fun leafs(): Iterable<FileNode> {
		return descendants().filter { it.isLeaf }
	}

	fun descendants(): Iterable<FileNode> {
		return children + children.flatMap { it.descendants() }
	}

	fun child(name: String): FileNode? {
		return when (name) {
			"" -> this
			"." -> this
			".." -> parent
			else -> children.firstOrNull { it.name == name }
		}
	}

	open protected fun _createChild(name: String, type: FileNodeType): FileNode {
		return FileNode(tree, this, name, type)
	}

	fun createChild(name: String, type: FileNodeType): FileNode {
		if (child(name) != null) throw FileAlreadyExistsException(File(name), reason = "Child $name already exists")
		return _createChild(name, type)
	}

	fun access(path: String, mustCreate: Boolean = false): FileNode {
		var node: FileNode = this
		var first = true

		for (name in path.split("/")) {
			var child = if (name == "" && first) root else node.child(name)
			if (child == null && mustCreate) {
				child = FileNode(tree, node, name, FileNodeType.DIRECTORY)
			}
			if (child == null) throw NoSuchFileException(File(path), reason = "Can't access '$path' (missing '$name')")
			node = child
			first = false
		}

		return node
	}

	operator fun get(path: String, mustCreate: Boolean = false): FileNode = access(path, mustCreate)

	fun remove() {
		if (parent != null) {
			parent.children.remove(this)
		}
	}
}
