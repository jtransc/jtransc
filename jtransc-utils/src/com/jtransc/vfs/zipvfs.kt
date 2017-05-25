package com.jtransc.vfs

import com.jtransc.io.indexOf
import com.jtransc.io.ra.RASlice
import com.jtransc.io.ra.RAStream
import com.jtransc.io.readAvailableChunk
import com.jtransc.io.readExactBytes
import com.jtransc.util.getBits
import com.jtransc.util.open
import com.jtransc.util.toIntClamp
import com.jtransc.util.toUInt
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

fun ZipVfs(file: File) = ZipVfs(file.open(), LocalVfs(file))
fun ZipVfs(file: String) = ZipVfs(File(file))
fun ZipVfs(data: ByteArray, zipFile: SyncVfsFile? = null) = ZipVfs(data.open(), zipFile)

fun ZipVfs(s: RAStream, zipFile: SyncVfsFile? = null): SyncVfsFile {
	//val s = zipFile.open(VfsOpenMode.READ)
	var endBytes = ByteArray(0)

	val PK_END = byteArrayOf(0x50, 0x4B, 0x05, 0x06)
	var pk_endIndex = -1

	for (chunkSize in listOf(0x16, 0x100, 0x1000, 0x10000)) {
		s.position = Math.max(0L, s.getLength() - chunkSize)
		endBytes = s.readBytesExact(Math.max(chunkSize, s.getAvailable().toIntClamp()))
		pk_endIndex = endBytes.indexOf(PK_END)
		if (pk_endIndex >= 0) break
	}

	if (pk_endIndex < 0) throw IllegalArgumentException("Not a zip file")

	val data = Arrays.copyOfRange(endBytes, pk_endIndex, endBytes.size).open()

	fun String.normalizeName() = this.trim('/')

	class ZipEntry(
		val path: String,
		val compressionMethod: Int,
		val isDirectory: Boolean,
		val time: DosFileDateTime,
		val offset: Int,
		val inode: Long,
		val headerEntry: RAStream,
		val compressedSize: Long,
		val uncompressedSize: Long
	)

	fun ZipEntry?.toStat(file: SyncVfsFile): SyncVfsStat {
		val vfs = file.vfs
		return if (this != null) {
			SyncVfsStat(file, isDirectory = isDirectory, size = uncompressedSize, inode = inode, mtime = this.time.javaDate, exists = true, isSymlink = false, mode = FileMode.FULL_ACCESS)
		} else {
			SyncVfsStat.notExists(file)
		}
	}

	val files = LinkedHashMap<String, ZipEntry>()
	val filesPerFolder = LinkedHashMap<String, LinkedHashMap<String, ZipEntry>>()

	data.apply {
		//println(s)
		if (readS32_BE() != 0x504B_0506) throw IllegalStateException("Not a zip file")
		val diskNumber = readU16_LE()
		val startDiskNumber = readU16_LE()
		val entriesOnDisk = readU16_LE()
		val entriesInDirectory = readU16_LE()
		val directorySize = readS32_LE()
		val directoryOffset = readS32_LE()
		val commentLength = readU16_LE()

		//println("Zip: $entriesInDirectory")

		val ds = s.sliceWithSize(directoryOffset.toLong(), directorySize.toLong()).readAvailableBytes().open()
		ds.apply {
			for (n in 0 until entriesInDirectory) {
				if (readS32_BE() != 0x504B_0102) throw IllegalStateException("Not a zip file record")
				val versionMade = readU16_LE()
				val versionExtract = readU16_LE()
				val flags = readU16_LE()
				val compressionMethod = readU16_LE()
				val fileTime = readU16_LE()
				val fileDate = readU16_LE()
				val crc = readS32_LE()
				val compressedSize = readS32_LE()
				val uncompressedSize = readS32_LE()
				val fileNameLength = readU16_LE()
				val extraLength = readU16_LE()
				val fileCommentLength = readU16_LE()
				val diskNumberStart = readU16_LE()
				val internalAttributes = readU16_LE()
				val externalAttributes = readS32_LE()
				val headerOffset = readS32_LE()
				val name = readStringz(fileNameLength, Charsets.UTF_8)
				val extra = readBytes(extraLength)

				val isDirectory = name.endsWith("/")
				val normalizedName = name.normalizeName()

				val baseFolder = normalizedName.substringBeforeLast('/', "")
				val baseName = normalizedName.substringAfterLast('/')

				val folder = filesPerFolder.getOrPut(baseFolder) { LinkedHashMap() }
				val entry = ZipEntry(
					path = name,
					compressionMethod = compressionMethod,
					isDirectory = isDirectory,
					time = DosFileDateTime(fileTime, fileDate),
					inode = n.toLong(),
					offset = headerOffset,
					headerEntry = s.sliceAvailable(headerOffset.toUInt()),
					compressedSize = compressedSize.toUInt(),
					uncompressedSize = uncompressedSize.toUInt()
				)
				val components = listOf("") + PathInfo(normalizedName).getFullComponents()
				for (m in 1 until components.size) {
					val f = components[m - 1]
					val c = components[m]
					if (c !in files) {
						val folder2 = filesPerFolder.getOrPut(f) { LinkedHashMap() }
						val entry2 = ZipEntry(path = c, compressionMethod = 0, isDirectory = true, time = DosFileDateTime(0, 0), inode = 0L, offset = 0, headerEntry = byteArrayOf().open(), compressedSize = 0L, uncompressedSize = 0L)
						folder2[PathInfo(c).basename] = entry2
						files[c] = entry2
					}
				}
				//println(components)
				folder[baseName] = entry
				files[normalizedName] = entry
			}
		}
		files[""] = ZipEntry(path = "", compressionMethod = 0, isDirectory = true, time = DosFileDateTime(0, 0), inode = 0L, offset = 0, headerEntry = byteArrayOf().open(), compressedSize = 0L, uncompressedSize = 0L)

		//for (folder in files) println(folder)
		Unit
	}

	class Impl : SyncVfs() {
		val vfs = this
		override val absolutePath: String get() = zipFile?.realpathOS ?: ""

		override fun read(path: String): ByteArray {
			val entry = files[path.normalizeName()] ?: throw FileNotFoundException("Path: '$path'")
			val base = entry.headerEntry.slice()
			return base.run {
				if (this.available < 16) throw IllegalStateException("Chunk to small to be a ZIP chunk")
				if (readS32_BE() != 0x504B_0304) throw IllegalStateException("Not a zip file")
				val version = readU16_LE()
				val flags = readU16_LE()
				val compressionType = readU16_LE()
				val fileTime = readU16_LE()
				val fileDate = readU16_LE()
				val crc = readS32_LE()
				val compressedSize = readS32_LE()
				val uncompressedSize = readS32_LE()
				val fileNameLength = readU16_LE()
				val extraLength = readU16_LE()
				val name = readStringz(fileNameLength, Charsets.UTF_8)
				val extra = readBytesExact(extraLength)
				val compressedData = readBytesExact(entry.compressedSize.toInt())

				when (entry.compressionMethod) {
				// Uncompressed
					0 -> compressedData
				// Deflate
					8 -> //InflaterInputStream(ByteArrayInputStream(compressedData), Inflater(true)).readBytes()
						InflaterInputStream(ByteArrayInputStream(compressedData), Inflater(true)).readExactBytes(entry.uncompressedSize.toInt())
					else -> TODO("Not implemented zip method ${entry.compressionMethod}")
				}
			}
		}

		/*
		private fun open(path: String): RAStream {
			val entry = files[path.normalizeName()] ?: throw FileNotFoundException("Path: '$path'")
			val base = entry.headerEntry.slice()
			return base.run {
				if (this.available < 16) throw IllegalStateException("Chunk to small to be a ZIP chunk")
				if (readS32_BE() != 0x504B_0304) throw IllegalStateException("Not a zip file")
				val version = readU16_LE()
				val flags = readU16_LE()
				val compressionType = readU16_LE()
				val fileTime = readU16_LE()
				val fileDate = readU16_LE()
				val crc = readS32_LE()
				val compressedSize = readS32_LE()
				val uncompressedSize = readS32_LE()
				val fileNameLength = readU16_LE()
				val extraLength = readU16_LE()
				val name = readStringz(fileNameLength, Charsets.UTF_8)
				val extra = readBytes(extraLength)
				val compressedData = readSlice(entry.compressedSize)

				when (entry.compressionMethod) {
				// Uncompressed
					0 -> compressedData
				// Deflate
					8 -> InflateRAStream(compressedData, Inflater(true), uncompressedSize.toLong())
					else -> TODO("Not implemented zip method ${entry.compressionMethod}")
				}
			}
		}
		*/

		override fun stat(path: String): SyncVfsStat {
			return files[path.normalizeName()].toStat(this@Impl[path])
		}

		override fun listdir(path: String): Iterable<SyncVfsStat> {
			val items = filesPerFolder[path.normalizeName()] ?: LinkedHashMap()
			val base = vfs[path]
			return items.map { it.value.toStat(base[it.key]) }
		}

		override fun toString(): String = "ZipVfs($zipFile)"
	}

	return Impl().root()
}

private class DosFileDateTime(var time: Int, var date: Int) {
	val seconds: Int get() = 2 * date.getBits(0, 5)
	val minutes: Int get() = 2 * date.getBits(5, 6)
	val hours: Int get() = 2 * date.getBits(11, 5)
	val day: Int get() = date.getBits(0, 5)
	val month: Int get() = date.getBits(5, 4)
	val year: Int get() = 1980 + date.getBits(9, 7)
	val utcTimestamp: Long by lazy { Date.UTC(year - 1900, month - 1, day, hours, minutes, seconds) }
	val javaDate: Date by lazy { Date(utcTimestamp) }
}

fun SyncVfsFile.openAsZip() = ZipVfs(this.read().open(), this)

/*
class InflateRAStream(val base: RASlice, val inflater: Inflater, val uncompressedSize: Long? = null) : RAStream() {
	override fun write(position: Long, ref: ByteArray?, pos: Int, len: Int) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun read(position: Long, ref: ByteArray, pos: Int, len: Int): Int {
		if (inflater.needsInput()) {
			inflater.setInput(base.readBytes(1024))
		}
		return inflater.inflate(ref, pos, len)
	}

	override fun setLength(value: Long) = throw UnsupportedOperationException()
	override fun getLength(): Long = uncompressedSize ?: throw UnsupportedOperationException()

	override fun close() {
		inflater.end()
	}
}
*/