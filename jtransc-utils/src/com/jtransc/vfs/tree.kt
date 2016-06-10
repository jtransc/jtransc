package com.jtransc.vfs

import com.jtransc.vfs.node.FileNodeIO
import com.jtransc.vfs.node.FileNodeTree
import com.jtransc.vfs.node.FileNodeType
import java.util.*

open class BaseTreeVfs(val tree: FileNodeTree) : SyncVfs() {
	protected val _root = tree.root

	override val absolutePath: String get() = ""
	override fun read(path: String): ByteArray {
		return _root.access(path).io?.read()!!
	}

	override fun write(path: String, data: ByteArray): Unit {
		val item = _root.access(path, true)
		var writtenData = data
		var writtenTime = Date()
		item.type = FileNodeType.FILE
		item.io = object : FileNodeIO() {
			override fun mtime(): Date = writtenTime
			override fun read(): ByteArray = writtenData
			override fun write(data: ByteArray) {
				writtenTime = Date()
				writtenData = data
			}

			override fun size(): Long = writtenData.size.toLong()
			override fun mode(): FileMode = FileMode.FULL_ACCESS
		}
	}

	override fun listdir(path: String): Iterable<SyncVfsStat> {
		return _root.access(path).map {
			it.toSyncStat(this, "${path}/${it.name}")
		}
	}

	override fun mkdir(path: String): Unit {
		_root.access(path, true)
	}

	override fun rmdir(path: String): Unit {
		try {
			val node = _root.access(path, false)
			node.remove()
		} catch (e: Throwable) {

		}
	}

	override fun exists(path: String): Boolean {
		try {
			_root.access(path)
			return true
		} catch(e: Throwable) {
			return false
		}
	}

	override fun remove(path: String): Unit = _root.access(path).remove()
	override fun stat(path: String): SyncVfsStat = _root.access(path).toSyncStat(this, path)

	override fun setMtime(path: String, time: Date) {
		// @TODO!
	}
}
