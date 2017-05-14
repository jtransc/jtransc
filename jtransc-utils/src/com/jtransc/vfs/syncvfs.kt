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

package com.jtransc.vfs

import com.jtransc.env.OS
import com.jtransc.error.*
import com.jtransc.io.readExactBytes
import com.jtransc.text.ToString
import com.jtransc.text.splitLast
import com.jtransc.vfs.node.FileNode
import com.jtransc.vfs.node.FileNodeTree
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

data class SyncVfsStat(
	val file: SyncVfsFile,
	val size: Long,
	val mtime: Date,
	val isDirectory: Boolean,
	val isSymlink: Boolean,
	val exists: Boolean,
	val mode: FileMode
) {
	val name: String get() = file.name
	val path: String get() = file.path
	val isFile: Boolean get() = !isDirectory

	companion object {
		fun notExists(file: SyncVfsFile) = SyncVfsStat(
			file = file,
			size = 0L,
			mtime = Date(),
			isDirectory = false,
			isSymlink = false,
			exists = false,
			mode = FileMode.FULL_ACCESS
		)
	}
}

class SyncVfsFile(internal val vfs: SyncVfs, val path: String) {
	val size: Long get() = stat().size
	val mtime: Date get() = stat().mtime
	fun setMtime(time: Date) = vfs.setMtime(path, time)
	fun stat(): SyncVfsStat = vfs.stat(path)
	fun chmod(mode: FileMode): Unit = vfs.chmod(path, mode)
	fun symlinkTo(target: String): Unit = vfs.symlink(path, target)
	fun read(): ByteArray = vfs.read(path)
	fun readBytes(): ByteArray = read()
	fun readOrNull(): ByteArray? = if (exists) read() else null
	inline fun <reified T : Any> readSpecial(): T = readSpecial(T::class.java)
	fun <T> readSpecial(clazz: Class<T>): T = vfs.readSpecial(clazz, path)
	fun write(data: ByteArray): Unit = vfs.write(path, data)
	fun readString(encoding: Charset = Charsets.UTF_8): String = encoding.toString(vfs.read(path))
	fun readLines(encoding: Charset = Charsets.UTF_8): List<String> = this.readString(encoding).lines()
	val exists: Boolean get() = vfs.exists(path)
	val isDirectory: Boolean get() = stat().isDirectory
	fun remove(): Unit = vfs.remove(path)
	fun removeIfExists(): Unit = if (exists) remove() else Unit

	fun exec(cmdAndArgs: List<String>, options: ExecOptions = ExecOptions()): ProcessResult = vfs.exec(path, cmdAndArgs.first(), cmdAndArgs.drop(1), options)
	fun exec(cmd: String, args: List<String>, options: ExecOptions): ProcessResult = vfs.exec(path, cmd, args, options)
	fun exec(cmd: String, args: List<String>, env: Map<String, String> = mapOf()): ProcessResult = exec(cmd, args, ExecOptions(passthru = false, env = env))
	fun exec(cmd: String, vararg args: String, env: Map<String, String> = mapOf()): ProcessResult = exec(cmd, args.toList(), ExecOptions(passthru = false, env = env))
	fun passthru(cmd: String, args: List<String>, filter: ((line: String) -> Boolean)? = null, env: Map<String, String> = mapOf()): ProcessResult = exec(cmd, args, ExecOptions(passthru = true, filter = filter, env = env))
	fun passthru(cmd: String, vararg args: String, filter: ((line: String) -> Boolean)? = null, env: Map<String, String> = mapOf()): ProcessResult = exec(cmd, args.toList(), ExecOptions(passthru = true, filter = filter, env = env))
	val name: String get() = path.substringAfterLast('/')
	val realpath: String get() = jailCombinePath(vfs.absolutePath, path)
	val realpathOS: String get() = if (OS.isWindows) realpath.replace('/', '\\') else realpath
	val realfile: File get() = File(realpathOS)
	fun listdir(): Iterable<SyncVfsStat> = vfs.listdir(path)
	fun listdirRecursive(): Iterable<SyncVfsStat> = listdirRecursive({ true })
	fun listdirRecursive(filter: (stat: SyncVfsStat) -> Boolean): Iterable<SyncVfsStat> {
		//log("REALPATH: ${this.realpath}")
		return listdir().flatMap {
			//log("item:${it.path}")
			if (filter(it)) {
				if (it.isDirectory) {
					//log("directory! ${it.path}")
					listOf(it) + it.file.listdirRecursive()
				} else {
					//log("file! ${it.path}")
					listOf(it)
				}
			} else {
				listOf()
			}
		}
	}

	fun firstRecursive(filter: (stat: SyncVfsStat) -> Boolean): SyncVfsStat {
		for (item in listdirRecursive()) {
			if (filter(item)) return item
		}
		invalidOp("No item on SyncVfsFile.firstRecursive")
	}

	fun mkdir(): Unit = vfs.mkdir(path)
	fun rmdir(): Unit = vfs.rmdir(path)
	fun ensuredir(): SyncVfsFile {
		if (path == "") return this
		if (!parent.exists) parent.ensuredir()
		mkdir()
		return this
	}

	fun ensureParentDir(): SyncVfsFile {
		parent.ensuredir()
		return this
	}

	fun rmdirRecursively(): Unit {
		for (item in this.listdir()) {
			if (item.isDirectory) {
				item.file.rmdirRecursively()
			} else {
				item.file.remove()
			}
		}
		this.rmdir()
	}

	fun rmdirRecursivelyIfExists(): Unit {
		if (exists) return rmdirRecursively();
	}

	fun access(path: String): SyncVfsFile = SyncVfsFile(vfs, combinePaths(this.path, path));

	operator fun get(path: String): SyncVfsFile = access(path)
	operator fun set(path: String, content: String) = set(path, content.toByteArray(UTF8))
	operator fun set(path: String, content: ToString) = set(path, content.toString().toByteArray(UTF8))
	operator fun set(path: String, content: SyncVfsFile) = set(path, content.readBytes())
	operator fun set(path: String, content: ByteArray) {
		val file = access(path).ensureParentDir()
		if (!file.exists || file.read() != content) {
			file.write(content)
		}
	}

	operator fun contains(path: String): Boolean = access(path).exists

	fun jailAccess(path: String): SyncVfsFile = access(path).jail()
	fun jail(): SyncVfsFile = AccessSyncVfs(vfs, path).root();
	override fun toString() = "SyncVfsFile($vfs, '$path')"

	fun write(data: String, encoding: Charset = UTF8): SyncVfsFile = writeString(data, encoding)

	fun writeString(data: String, encoding: Charset = UTF8): SyncVfsFile {
		write(data.toByteArray(encoding))
		return this
	}

	fun dumpTree() {
		println("<dump>")
		for (path in listdirRecursive()) {
			println(path)
		}
		println("</dump>")
	}

	fun copyTo(that: SyncVfsFile): Unit = that.ensureParentDir().write(this.read())

	fun copyTreeTo(that: SyncVfsFile, filter: (from: SyncVfsFile, to: SyncVfsFile) -> Boolean = { from, to -> true }, doLog: Boolean = true): Unit {
		if (doLog) com.jtransc.log.log("copyTreeTo " + this.realpath + " -> " + that.realpath)
		val stat = this.stat()
		if (stat.isDirectory) {
			that.mkdir()
			for (node in this.listdir()) {
				node.file.copyTreeTo(that[node.name], filter, doLog = doLog)
			}
		} else {
			this.copyTo(that)
		}
	}


	fun toDumpString(): String {
		return listdirRecursive().filter { it.isFile }.map { "// ${it.path}:\n${it.file.readString()}" }.joinToString("\n")
	}
}

data class ExecOptions(
	val passthru: Boolean = false,
	val filter: ((line: String) -> Boolean)? = null,
	val env: Map<String, String> = mapOf(),
    val sysexec: Boolean = false,
	val fixencoding: Boolean = true,
	val fixLineEndings: Boolean = true
) {
	val redirect: Boolean get() = passthru
}

open class SyncVfs {
	final fun root() = SyncVfsFile(this, "")

	open val absolutePath: String = ""
	open fun read(path: String): ByteArray {
		throw NotImplementedException()
	}

	open fun <T> readSpecial(clazz: Class<T>, path: String): T {
		throw NotImplementedException()
	}

	open fun write(path: String, data: ByteArray): Unit {
		throw NotImplementedException()
	}

	open fun listdir(path: String): Iterable<SyncVfsStat> {
		throw NotImplementedException()
	}

	open fun mkdir(path: String): Unit {
		throw NotImplementedException()
	}

	open fun rmdir(path: String): Unit {
		throw NotImplementedException()
	}

	open fun exec(path: String, cmd: String, args: List<String>, options: ExecOptions): ProcessResult {
		throw NotImplementedException()
	}

	open fun exists(path: String): Boolean {
		try {
			return stat(path).exists
		} catch (e: Throwable) {
			return false
		}
	}

	open fun remove(path: String): Unit {
		throw NotImplementedException()
	}

	open fun stat(path: String): SyncVfsStat {
		val file = SyncVfsFile(this, path)
		return try {
			val data = read(path)
			SyncVfsStat(
				file = file,
				size = data.size.toLong(),
				mtime = Date(),
				isDirectory = false,
				exists = true,
				isSymlink = false,
				mode = FileMode.FULL_ACCESS
			)
		} catch (e: IOException) {
			SyncVfsStat.notExists(file)
		}
		//throw NotImplementedException()
	}

	open fun chmod(path: String, mode: FileMode): Unit {
		throw NotImplementedException()
	}

	open fun symlink(link: String, target: String): Unit {
		throw NotImplementedException()
	}

	open fun setMtime(path: String, time: Date) {
		throw NotImplementedException()
	}
}

fun FileNode.toSyncStat(vfs: SyncVfs, path: String): SyncVfsStat {
	return SyncVfsStat(
		file = SyncVfsFile(vfs, path),
		size = this.size(),
		mtime = this.mtime(),
		isDirectory = this.isDirectory(),
		isSymlink = this.isSymlink(),
		exists = true,
		mode = this.mode()
	)
}


private class _MemoryVfs : BaseTreeVfs(FileNodeTree()) {
}

private class _LocalVfs : SyncVfs() {
	override val absolutePath: String get() = ""
	override fun read(path: String): ByteArray = RawIo.fileRead(path)
	override fun write(path: String, data: ByteArray): Unit = RawIo.fileWrite(path, data)
	override fun listdir(path: String): Iterable<SyncVfsStat> = RawIo.listdir(path).map { it.toSyncStat(this, "$path/${it.name}") }
	override fun mkdir(path: String): Unit = RawIo.mkdir(path)
	override fun rmdir(path: String): Unit = RawIo.rmdir(path)
	override fun exec(path: String, cmd: String, args: List<String>, options: ExecOptions): ProcessResult = RawIo.execOrPassthruSync(path, cmd, args, options)
	override fun exists(path: String): Boolean = RawIo.fileExists(path)
	override fun remove(path: String): Unit = RawIo.fileRemove(path)
	override fun stat(path: String): SyncVfsStat = RawIo.fileStat(path).toSyncStat(this, path)
	override fun chmod(path: String, mode: FileMode): Unit = Unit.apply { RawIo.chmod(path, mode) }
	override fun symlink(link: String, target: String): Unit = RawIo.symlink(link, target)
	override fun setMtime(path: String, time: Date) = RawIo.setMtime(path, time)
}

fun File.toSyncStat(vfs: SyncVfs, path: String) = SyncVfsStat(
	file = SyncVfsFile(vfs, path),
	size = this.length(),
	mtime = Date(this.lastModified()),
	isDirectory = this.isDirectory,
	//isSymlink = java.nio.file.Files.isSymbolicLink(java.nio.file.Paths.get(this.toURI())),
	isSymlink = false,
	exists = true,
	mode = FileMode.FULL_ACCESS
)

abstract class ProxySyncVfs : SyncVfs() {
	abstract protected fun transform(path: String): SyncVfsFile
	open protected fun transformStat(stat: SyncVfsStat): SyncVfsStat = stat

	override val absolutePath: String get() = transform("").realpath
	override fun read(path: String): ByteArray = transform(path).read()
	override fun <T> readSpecial(clazz: Class<T>, path: String): T = transform(path).readSpecial(clazz)
	override fun write(path: String, data: ByteArray): Unit = transform(path).write(data)
	// @TODO: Probably transform SyncVfsStat!
	override fun listdir(path: String): Iterable<SyncVfsStat> = transform(path).listdir().map { transformStat(it) }

	override fun mkdir(path: String): Unit {
		transform(path).mkdir()
	}

	override fun rmdir(path: String): Unit {
		transform(path).rmdir()
	}

	override fun exec(path: String, cmd: String, args: List<String>, options: ExecOptions): ProcessResult = transform(path).exec(cmd, args, options)
	override fun exists(path: String): Boolean = transform(path).exists
	override fun remove(path: String): Unit = transform(path).remove()
	override fun stat(path: String): SyncVfsStat = transformStat(transform(path).stat())
	override fun chmod(path: String, mode: FileMode): Unit = transform(path).chmod(mode)
	override fun symlink(link: String, target: String): Unit = transform(link).symlinkTo(transform(target).path)
	override fun setMtime(path: String, time: Date) = transform(path).setMtime(time)
}

// @TODO: paths should not start with "/"
private class AccessSyncVfs(val parent: SyncVfs, val path: String) : ProxySyncVfs() {
	override fun transform(path: String): SyncVfsFile = SyncVfsFile(parent, jailCombinePath(this.path, path))
	override fun transformStat(stat: SyncVfsStat): SyncVfsStat {
		// @TODO: Do this better!
		val statFilePath = "/" + stat.file.path.trimStart('/')
		val thisPath = "/" + this.path.trimStart('/')
		if (!statFilePath.startsWith(thisPath)) {
			throw InvalidOperationException("Assertion failed $statFilePath must start with $thisPath")
		}

		return SyncVfsStat(
			file = SyncVfsFile(this, "/" + statFilePath.removePrefix(thisPath)),
			size = stat.size,
			mtime = stat.mtime,
			isDirectory = stat.isDirectory,
			isSymlink = stat.isSymlink,
			exists = true,
			mode = stat.mode
		)
	}

	override fun toString(): String = "AccessSyncVfs($parent, $path)"
}

private class _LogSyncVfs(val parent: SyncVfs) : ProxySyncVfs() {
	override fun transform(path: String): SyncVfsFile = SyncVfsFile(parent, path)

	override fun write(path: String, data: ByteArray): Unit {
		println("Writting $parent($path) with ${data.toString(UTF8)}")
		super.write(path, data)
	}
}


private class _UrlVfs : SyncVfs() {
	override fun read(path: String): ByteArray {
		val fixedUrl = Regex("^http(s?):/([^/])").replace(path, "http$1://$2")
		val connection = URL(fixedUrl).openConnection()
		connection.allowUserInteraction = false
		connection.connectTimeout = 10000
		connection.readTimeout = 10000
		connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
		connection.addRequestProperty("User-Agent", "Mozilla");
		//connection.addRequestProperty("Referer", fixedUrl);
		//println(connection.headerFields)
		val sin = connection.inputStream;
		val sout = ByteArrayOutputStream();
		sin.copyTo(sout)
		return sout.toByteArray()
	}
}

fun RootUrlVfs(): SyncVfsFile = _UrlVfs().root()
fun UrlVfs(url: String): SyncVfsFile = _UrlVfs().root().jailAccess(url)
fun UrlVfs(url: URL): SyncVfsFile = _UrlVfs().root().jailAccess(url.toExternalForm())
fun RootLocalVfs(): SyncVfsFile = _LocalVfs().root()
fun MergeVfs(nodes: List<SyncVfsFile>) = if (nodes.isNotEmpty()) MergedSyncVfs(nodes).root() else MemoryVfs()
fun MergedLocalAndJars(paths: List<String>) = MergeVfs(LocalAndJars(paths))
fun LocalAndJars(paths: List<String>): List<SyncVfsFile> {
	return paths.map { if (it.endsWith(".jar")) ZipVfs(it) else LocalVfs(File(it)) }
}

fun CompressedVfs(file: File): SyncVfsFile {
	val npath = file.absolutePath.toLowerCase()

	return if (npath.endsWith(".tar.gz")) {
		TarVfs(GZIPInputStream(file.inputStream()).readBytes())
	} else if (npath.endsWith(".zip") || npath.endsWith(".jar")) {
		ZipVfs(file)
	} else if (npath.endsWith(".tar")) {
		TarVfs(file)
	} else {
		invalidOp("Don't know how to handle compressed file: $file")
	}
}


fun ZipVfs(path: String): SyncVfsFile = ZipSyncVfs(ZipFile(path)).root()
fun ZipVfs(file: File): SyncVfsFile = ZipSyncVfs(ZipFile(file)).root()
fun ResourcesVfs(clazz: Class<*>): SyncVfsFile = ResourcesSyncVfs(clazz).root()
@Deprecated("Use File instead", ReplaceWith("LocalVfs(File(path))", "java.io.File"))
fun LocalVfs(path: String): SyncVfsFile = RootLocalVfs().access(path).jail()

fun UnjailedLocalVfs(file: File): SyncVfsFile = RootLocalVfs().access(file.absolutePath)

fun LocalVfs(file: File): SyncVfsFile = _LocalVfs().root().access(file.absolutePath).jail()
fun LocalVfsEnsureDirs(file: File): SyncVfsFile {
	ignoreErrors { file.mkdirs() }
	return _LocalVfs().root().access(file.absolutePath).jail()
}

fun CwdVfs(): SyncVfsFile = LocalVfs(File(RawIo.cwd()))
fun CwdVfs(path: String): SyncVfsFile = CwdVfs().jailAccess(path)
fun ScriptVfs(): SyncVfsFile = LocalVfs(File(RawIo.script()))
fun MemoryVfs(): SyncVfsFile = _MemoryVfs().root()
fun MemoryVfs(vararg files: Pair<String, String>): SyncVfsFile {
	val vfs = _MemoryVfs().root()
	for (file in files) {
		vfs.access(file.first).ensureParentDir().writeString(file.second)
	}
	return vfs
}

fun GetClassJar(clazz: Class<*>): File {
	val classLoader = VfsPath::class.java.classLoader
	val classFilePath = clazz.name.replace('.', '/') + ".class"
	//println(classFilePath)
	//println(classLoader)
	val classUrl = classLoader.getResource(classFilePath)
	//path.
	//println(path)

	val regex = Regex("^file:(.*?)!(.*?)$")
	val result = regex.find(classUrl.path)!!
	val jarPath = result.groups[1]!!.value

	return File(jarPath)
}

fun MemoryVfsBin(vararg files: Pair<String, ByteArray>): SyncVfsFile {
	val vfs = _MemoryVfs().root()
	for (file in files) {
		vfs.access(file.first).ensureParentDir().write(file.second)
	}
	return vfs
}

fun MemoryVfsFile(content: String, name: String = "file"): SyncVfsFile {
	return MemoryVfs(name to content).access(name)
}

fun MemoryVfsFileBin(content: ByteArray, name: String = "file"): SyncVfsFile {
	return MemoryVfsBin(name to content).access(name)
}

fun LogVfs(parent: SyncVfsFile): SyncVfsFile = _LogSyncVfs(parent.jail().vfs).root()
fun SyncVfsFile.log() = LogVfs(this)


fun normalizePath(path: String): String {
	val out = ArrayList<String>();
	for (chunk in path.replace('\\', '/').split('/')) {
		when (chunk) {
			".." -> if (out.size > 0) out.removeAt(0)
			"." -> Unit
			"" -> if (out.size == 0) out.add("")
			else -> out.add(chunk)
		}
	}
	return out.joinToString("/")
}

fun combinePaths(vararg paths: String): String {
	return normalizePath(paths.filter { it != "" }.joinToString("/"))
}

fun jailCombinePath(base: String, access: String): String {
	return combinePaths(base, normalizePath(access))
}

class VfsPath(val path: String)


data class UserKey<T>(val name: String)

inline fun <reified T : Any> UserKey(): UserKey<T> = UserKey<T>(T::class.java.name)

interface IUserData {
	operator fun <T : Any> contains(key: UserKey<T>): Boolean
	operator fun <T : Any?> get(key: UserKey<T>): T?
	operator fun <T : Any> set(key: UserKey<T>, value: T)
}

fun <T : Any> IUserData.getCached(key: UserKey<T>, builder: () -> T): T {
	if (key !in this) this[key] = builder()
	return this[key]!!
}

@Suppress("UNCHECKED_CAST")
class UserData : IUserData {
	private val dict = hashMapOf<Any, Any>()

	override operator fun <T : Any> contains(key: UserKey<T>): Boolean = key in dict
	override operator fun <T : Any?> get(key: UserKey<T>): T? = dict[key] as T?
	override operator fun <T : Any> set(key: UserKey<T>, value: T) {
		dict.put(key, value)
	}
}


object Path {
	fun parent(path: String): String = if (path.contains('/')) path.substringBeforeLast('/') else ""
	fun withoutExtension(path: String): String = path.substringBeforeLast('.')
	fun withExtension(path: String, ext: String): String = withoutExtension(path) + ".$ext"
	fun withBaseName(path: String, name: String): String = "${parent(path)}/$name"
}

val SyncVfsFile.parent: SyncVfsFile get() {
	//println("Path: '${path}', Parent: '${Path.parent(path)}'")
	return SyncVfsFile(vfs, Path.parent(path))
}
val SyncVfsFile.withoutExtension: SyncVfsFile get() = SyncVfsFile(vfs, Path.withoutExtension(path))
fun SyncVfsFile.withExtension(ext: String): SyncVfsFile = SyncVfsFile(vfs, Path.withExtension(path, ext))
fun SyncVfsFile.withBaseName(baseName: String): SyncVfsFile = parent.access(baseName)

private class MergedSyncVfs(val nodes: List<SyncVfsFile>) : SyncVfs() {
	init {
		if (nodes.isEmpty()) throw InvalidArgumentException("Nodes can't be empty")
	}

	override val absolutePath: String = "#merged#"

	//private val nodesSorted = nodes.reversed()
	val nodesSorted = nodes

	private fun <T> op(path: String, act: String, action: (node: SyncVfsFile) -> T): T {
		var lastError: Throwable? = null
		for (node in nodesSorted) {
			try {
				return action(node)
			} catch(t: Throwable) {
				lastError = t
			}
		}
		throw RuntimeException("Can't $act file '$path' : $lastError")
	}

	override fun read(path: String): ByteArray = op(path, "read") { it[path].read() }
	override fun write(path: String, data: ByteArray) = op(path, "write") { it[path].write(data) }
	override fun <T> readSpecial(clazz: Class<T>, path: String): T = op(path, "readSpecial") { it[path].readSpecial(clazz) }
	override fun listdir(path: String): Iterable<SyncVfsStat> {
		//op(path, "listdir") { it[path].listdir() }
		return nodesSorted.flatMap { it.listdir() }
	}
	override fun mkdir(path: String) = op(path, "mkdir") { it[path].mkdir() }
	override fun rmdir(path: String) = op(path, "rmdir") { it[path].rmdir() }
	override fun exec(path: String, cmd: String, args: List<String>, options: ExecOptions): ProcessResult {
		return op(path, "exec") { it[path].exec(cmd, args, options) }
	}

	override fun remove(path: String) = op(path, "remove") { it[path].remove() }
	override fun stat(path: String): SyncVfsStat {
		var lastStat: SyncVfsStat? = null
		for (node in nodesSorted) {
			val stat = node[path].stat()
			lastStat = stat
			if (stat.exists) break
			//println(stat)
		}
		return lastStat!!
	}

	override fun setMtime(path: String, time: Date) = op(path, "setMtime") { it[path].setMtime(time) }

	override fun toString(): String = "MergedSyncVfs(" + this.nodes.joinToString(", ") + ")"
}

private class ResourcesSyncVfs(val clazz: Class<*>) : SyncVfs() {
	val classLoader = clazz.classLoader
	override fun read(path: String): ByteArray {
		return classLoader.getResourceAsStream(path).readBytes()
	}
}

private class ZipSyncVfs(val zip: ZipFile) : SyncVfs() {
	constructor(path: String) : this(ZipFile(path))

	constructor(file: File) : this(ZipFile(file))

	override val absolutePath: String = zip.name + "#"

	override fun read(path: String): ByteArray {
		val entry = zip.getEntry(path) ?: throw FileNotFoundException(path)
		val inputStream = zip.getInputStream(entry)
		return inputStream.readExactBytes(entry.size.toInt())
	}

	class Node(val zip: ZipSyncVfs, val name: String, val parent: Node? = null) {
		val path: String = (if (parent != null) "${parent.path}/$name" else name).trim('/')
		var entry: ZipEntry? = null
		val root: Node = parent?.root ?: this

		val stat: SyncVfsStat by lazy {
			SyncVfsStat(
				file = SyncVfsFile(zip, path),
				size = entry?.size ?: 0,
				mtime = Date(entry?.time ?: 0),
				isDirectory = entry?.isDirectory ?: true,
				isSymlink = false,
				exists = true,
				mode = FileMode.FULL_ACCESS
			)
		}

		init {
			parent?.children?.put(name, this)
		}

		val children = hashMapOf<String, Node>()

		fun createChild(name: String): Node {
			return Node(zip, name, this)
		}

		fun access(path: String, create: Boolean = false): Node {
			var current = if (path.startsWith("/")) root else this
			for (part in path.trim('/').split('/')) {
				when (part) {
					"", "." -> Unit
					".." -> current = current.parent ?: current
					else -> {
						var childNode = current.children[part]
						if (childNode == null && create) {
							childNode = Node(zip, part, current)
						}
						current = childNode!!
					}
				}
			}
			return current
		}
	}

	private val rootNode = Node(this, "")

	init {
		val cache = hashMapOf<String, Node>()
		for (e in zip.entries()) {
			val normalizedName = e.name.trim('/')
			val (path, name) = normalizedName.splitLast('/')
			//println("$path :: $name")
			if (path !in cache) {
				cache[path] = rootNode.access(path, create = true)
			}
			//println(cache[path]?.path)
			cache[path]!!.createChild(name).apply {
				this.entry = e
			}
		}
	}

	override fun listdir(path: String): Iterable<SyncVfsStat> {
		return rootNode.access(path).children.values.map { it.stat }
	}

	override fun stat(path: String): SyncVfsStat {
		return try {
			rootNode.access(path).stat
		} catch (e: Throwable) {
			SyncVfsStat.notExists(SyncVfsFile(this, path))
		}
	}

	override fun toString(): String = "ZipSyncVfs(${this.zip.name})"
}

fun SyncVfsFile.getUnmergedFiles(): List<SyncVfsFile> {
	val vfs = this.vfs
	if (vfs is MergedSyncVfs) {
		return vfs.nodes.map { it[this.path] }
	} else {
		return listOf(this)
	}
}