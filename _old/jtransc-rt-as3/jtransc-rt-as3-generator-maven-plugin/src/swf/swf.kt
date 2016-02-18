package swf

import com.jtransc.error.InvalidOperationException
import com.jtransc.util.Bits
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.DataFormatException
import java.util.zip.Inflater
import java.util.zip.ZipFile

object SwfTools {
	fun extractAbc(swf: ByteBuffer): ByteBuffer = Swf().read(SwfReader(swf)).abcBlocks.first()
}

class Swf {
	fun readSwc(r: File): Swf {
		val zip = ZipFile(r)
		val libraryswf = zip.getInputStream(zip.getEntry("library.swf")).use { it.readBytes() }
		return read(SwfReader(ByteBuffer.wrap(libraryswf)))
	}

	fun read(r: SwfReader): Swf {
		val type = when (r.u8().toChar()) {
			'F' -> CompressionType.NONE
			'C' -> CompressionType.ZLIB
			'Z' -> CompressionType.LZMA
			else -> throw InvalidOperationException("Invalid swf")
		}
		val type2 = r.u8().toChar()
		val type3 = r.u8().toChar()
		assert(type2 == 'W' && type3 == 'S')
		val version = r.u8()
		//println("version:$version")
		val fileLength = r.s32()
		//println("fileLength:$fileLength")

		val data = when (type) {
			CompressionType.NONE -> r.readAvailableBytes()
			CompressionType.ZLIB -> ByteBuffer.wrap(uncompress(r.readAvailableBytes().toArray()))
			else -> throw Exception("Unsupported compression")
		}
		readHeader(SwfReader(data))
		return this
	}

	fun readHeader(r: SwfReader) {
		val rect = r.rect()
		//println(rect)
		val frameRate = r.u16().toDouble() / 256.0
		val frameCount = r.u16()
		//println("frameRate: $frameRate, frameCount: $frameCount")
		while (r.available > 0) {
			val tagCodeAndLength = r.u16()
			var size = Bits.extract(tagCodeAndLength, 0, 6)
			val type = Bits.extract(tagCodeAndLength, 6, 10)
			if (size >= 63) {
				size = r.s32()
			}
			val content = r.bytes(size)
			readFrame(type, SwfReader(content))
		}
	}

	fun readFrame(tag: Int, r: SwfReader) {
		//println("tag:$tag, length:${r.length}")
		when (tag) {
			SwfTagTypes.DoABC2 -> {
				//println("abc2")
				readAbc2(r)
			}
			else -> {
				//println("else")
			}
		}
	}

	val abcBlocks = arrayListOf<ByteBuffer>()

	fun readAbc2(r: SwfReader) {
		val flags = r.s32()
		val name = r.stringz()
		val abc = r.readAvailableBytes()
		abcBlocks.add(abc)
		//readAbc2_2(SwfReader(abc))
		//println("flags: $flags")
		//println("name: $name")
	}

	private fun readAbc2_2(r: SwfReader) {
	}
}

class SwfReader(_data: ByteBuffer) {
	//val data = _data.order(ByteOrder.BIG_ENDIAN)
	val data = _data.order(ByteOrder.LITTLE_ENDIAN)
	var offset: Int = 0

	val length: Int get() = data.limit()
	val available: Int get() = length - offset
	val hasMore: Boolean get() = available > 0

	var _bitBuffer: Int = 0
	var _bitLength: Int = 0

	fun readAvailableBytes(): ByteBuffer = bytes(available)

	fun namespace_info(strings: List<String>): SwfNamespaceInfo {
		val type = u8()
		val name = strings[u30()]

		return when (type) {
			NamespaceKind.CONSTANT_Namespace -> SwfNamespace(name)
			NamespaceKind.CONSTANT_PackageNamespace -> SwfPackageNamespace(name)
			NamespaceKind.CONSTANT_PackageInternalNs -> SwfInternalNamespace(name)
			NamespaceKind.CONSTANT_ProtectedNamespace -> SwfProtectedNamespace(name)
			NamespaceKind.CONSTANT_ExplicitNamespace -> SwfExplicitNamespace(name)
			NamespaceKind.CONSTANT_StaticProtectedNs -> SwfStaticProtectedNamespace(name)
			NamespaceKind.CONSTANT_PrivateNs -> SwfPrivateNamespace(name)
			else -> throw InvalidOperationException()
		}
	}

	fun ns_set_info(namespaces: List<SwfNamespaceInfo>): SwfNsSetInfo {
		return SwfNsSetInfo((0..u30() - 1).map { namespaces[u30()] })
	}

	fun multiname_info(strings: List<String>, namespaces: List<SwfNamespaceInfo>, ns_sets: List<SwfNsSetInfo>): SwfMultiname {
		val kind = u8()
		return when (kind) {
			MultinameKind.CONSTANT_QName, MultinameKind.CONSTANT_QNameA -> QName(namespaces[u30()], strings[u30()])
			MultinameKind.CONSTANT_RTQName, MultinameKind.CONSTANT_RTQNameA -> RTQName(strings[u30()])
			MultinameKind.CONSTANT_RTQNameL, MultinameKind.CONSTANT_RTQNameLA -> RTQNameL()
			MultinameKind.CONSTANT_Multiname, MultinameKind.CONSTANT_MultinameA -> Multiname(strings[u30()], ns_sets[u30()])
			MultinameKind.CONSTANT_MultinameL, MultinameKind.CONSTANT_MultinameLA -> MultinameL(ns_sets[u30()])
			else -> throw InvalidOperationException()
		}
	}

	fun string_info(): String {
		val count = this.u30()
		val data = this.bytes(count)
		return Charsets.UTF_8.decode(data).toString()
	}

	fun stringz(): String {
		val out = ByteArrayOutputStream()
		while (available > 0) {
			val char = u8()
			if (char == 0) break
			out.write(char)
		}
		return Charsets.UTF_8.decode(ByteBuffer.wrap(out.toByteArray())).toString()
	}

	fun bytes(count: Int): ByteBuffer {
		val out = ByteBuffer.allocate(count)
		val offset = move(count)
		for (n in 0..count - 1) out.put(n, this.data[offset + n])
		return out
	}

	fun rect(): SwfRect {
		val nbits = bits(5)
		//println("nbits:$nbits")
		return SwfRect(bits(nbits), bits(nbits), bits(nbits), bits(nbits))
	}

	fun sbits(size: Int): Int {
		return (this.bits(size) shl (32 - size)) ushr (32 - size);
	}

	fun bits(size: Int): Int {
		while (size > _bitLength) {
			_bitBuffer = (_bitBuffer shl 8) or this.u8()
			_bitLength += 8
		}
		_bitLength -= size
		return (_bitBuffer ushr _bitLength) and Bits.mask(size)
	}

	fun readFixedBits(size: Int): Int {
		return this.sbits(size) / 65536
	}

	fun bitpad() {
		_bitBuffer = 0
		_bitLength = 0
	}

	private fun move(count: Int): Int {
		if (offset > length - count) throw IndexOutOfBoundsException()
		//println(":v$offset + $count")
		val old = this.offset
		this.offset += count
		return old
	}

	fun u8(): Int = s8() and 0xFF
	fun u16(): Int = s16() and 0xFFFF

	fun s8(): Int = data.get(move(1)).toInt()
	fun s16(): Int = data.getShort(move(2)).toInt()
	fun s32(): Int = data.getInt(move(4)).toInt()
	fun f32(): Float = data.getFloat(move(4)).toFloat()
	fun d64(): Double = data.getDouble(move(8)).toDouble()

	fun v_s32(): Int {
		/*
		var i:Int = 0
		var ret:Long = 0L
		var bytePos = 0
		var byteCount = 0
		var nextByte:Boolean = false
		do {
			i = u8();
			nextByte = (i shl 7) == 1
			i = i and 0x7f
			ret = ret or ((i.toLong()) shl bytePos)
			byteCount++;
			bytePos += 7;
		} while (nextByte && byteCount < 5);

		return i.toInt()
		*/
		var out: Int = 0
		var offset: Int = 0
		var byteCount = 0

		//println("------")
		while (available > 0 && byteCount < 5) {
			var data = u8()
			//println("u30:$data")

			//println(byteCount)
			if (false) {
				out = out shl 7
				out = out or (data and 0x7f)
			} else {
				out = out or ((data and 0x7f) shl offset)
				offset += 7
			}

			if ((data and 0x80) == 0) break
			byteCount++
		}
		return out
	}

	fun u30(): Int {
		return v_s32()
	}
}

fun uncompress(bytes: ByteArray): ByteArray {
	val decompressor = Inflater();
	decompressor.setInput(bytes);//feed the Inflater the bytes

	val stream = ByteArrayOutputStream(bytes.size());//an expandable byte array to store the uncompressed data

	val buffer = ByteArray(1024)
	while ( !decompressor.finished() )//read until the end of the stream is found
	{
		try {
			val count = decompressor.inflate(buffer);//decompress the data into the buffer
			stream.write(buffer, 0, count);
		} catch(dfe: DataFormatException) {
			dfe.printStackTrace();
		}
	}

	stream.close();

	return stream.toByteArray()
}


fun ByteBuffer.toArray(): ByteArray {
	val out = ByteArray(this.limit())
	for (n in 0..out.size() - 1) out[n] = this[n]
	return out
}


data class SwfRect(val xmin: Int, val xmax: Int, val ymin: Int, val ymax: Int)
interface SwfNamespaceInfo {
	val name: String
}

enum class SwfMethodType { Normal, Setter, Getter, Function }

interface SwfTrait
data class SwfTrait_Method(val id: Int, val method: SwfMethodInfo, val kind: SwfMethodType) : SwfTrait
data class SwfTrait_Slot_Const(val slot_id: Int, val type_name: SwfMultiname, val vindex: Int, val vkind: Int, val isConst: Boolean) : SwfTrait
data class SwfTrait_Function(val slot_id: Int, val function: SwfMethodInfo) : SwfTrait
data class SwfTrait_Class(val slot_id: Int, val classi: Int) : SwfTrait

object SwfTrait_Invalid : SwfTrait

data class SwfNamespace(override val name: String) : SwfNamespaceInfo
data class SwfPackageNamespace(override val name: String) : SwfNamespaceInfo
data class SwfInternalNamespace(override val name: String) : SwfNamespaceInfo
data class SwfProtectedNamespace(override val name: String) : SwfNamespaceInfo
data class SwfExplicitNamespace(override val name: String) : SwfNamespaceInfo
data class SwfStaticProtectedNamespace(override val name: String) : SwfNamespaceInfo
data class SwfPrivateNamespace(override val name: String) : SwfNamespaceInfo

object DummyNamespace : SwfNamespaceInfo {
	override val name = ""
}

data class SwfNsSetInfo(val namespaces: List<SwfNamespaceInfo>)
interface SwfMultiname

object NullMultiname : SwfMultiname

data class QName(val ns: SwfNamespaceInfo, val name: String) : SwfMultiname {
	override fun toString() = if ("$ns" != "") "$ns.$name" else "$name"
}

data class RTQName(val name: String) : SwfMultiname
class RTQNameL() : SwfMultiname
data class Multiname(val name: String, val ns_set: SwfNsSetInfo) : SwfMultiname {
	override fun toString() = "$name:$ns_set"
}

data class MultinameL(val ns_set: SwfNsSetInfo) : SwfMultiname
enum class TraitContext {
	Class, Instance, Script, Body;

	val isStatic: Boolean get() = this == Class
}

data class Trait(val context: TraitContext, val name: SwfMultiname, val type: Int, val flags: Int, val data: SwfTrait, val metadata: List<SwfMetaDataInfo?>)
data class ExceptionInfo(val from: Int, val to: Int, val target: Int, val exc_type: Int, val var_name: Int)
data class MethodBody(
	val method: Int,
	val maxstack: Int,
	val localcount: Int,
	val initscopedepth: Int,
	val maxscopedepth: Int,
	val code: ByteBuffer,
	val exceptions: List<ExceptionInfo>,
	val traits: List<Trait>
)

data class SwfMethodInfo(
	val name: String,
	val returntype: SwfMultiname,
	val params: List<Pair<String?, SwfMultiname>>,
	val flags: Int,
	val options: List<Pair<Int, Int>>
) {
	var body: MethodBody? = null

	val needArguments = (flags and 0x01) != 0
	val needActivation = (flags and 0x02) != 0
	val needRest = (flags and 0x04) != 0
	val hasOptional = (flags and 0x08) != 0
	val setDxns = (flags and 0x40) != 0
	val hasParamNames = (flags and 0x80) != 0
}

data class SwfMetaDataInfo(
	val name: String,
	val items: List<Pair<String, String>>
)

object TraitType {
	val Trait_Slot = 0
	val Trait_Method = 1
	val Trait_Getter = 2
	val Trait_Setter = 3
	val Trait_Class = 4
	val Trait_Function = 5
	val Trait_Const = 6
}

object TraitAttributes {
	val ATTR_Final = 0x1
	val ATTR_Override = 0x2
	val ATTR_Metadata = 0x4
}

object NamespaceKind {
	val CONSTANT_PrivateNs = 0x05 // 5
	val CONSTANT_Namespace = 0x08 // 8
	val CONSTANT_PackageNamespace = 0x16 // 22
	val CONSTANT_PackageInternalNs = 0x17 // 23
	val CONSTANT_ProtectedNamespace = 0x18 // 24
	val CONSTANT_ExplicitNamespace = 0x19 / 25
	val CONSTANT_StaticProtectedNs = 0x1A // 26
}

data class ConstantPool(
	val ints: List<Int>,
	val uints: List<Int>,
	val doubles: List<Double>,
	val strings: List<String>,
	val namespaces: List<SwfNamespaceInfo>,
	val ns_sets: List<SwfNsSetInfo>,
	val multinames: List<SwfMultiname>
)

data class SwfInstanceInfo(
	val name: SwfMultiname,
	val supername: SwfMultiname,
	val flags: Int,
	val protectedNs: SwfNamespaceInfo?,
	val interfaces: List<SwfMultiname>,
	val iinit: SwfMethodInfo,
	val traits: List<Trait>
) {
	val isSealed = (flags and 0x01) != 0
	val isFinal = (flags and 0x02) != 0
	val isInterface = (flags and 0x04) != 0
}

data class SwfClassInfo(
	val cinit: SwfMethodInfo,
	val traits: List<Trait>
)

data class SwfScript(
	val cinit: SwfMethodInfo,
	val traits: List<Trait>
)

data class SwfClass(val instance: SwfInstanceInfo, val clazz: SwfClassInfo)

object MultinameKind {
	val CONSTANT_QName = 0x07
	val CONSTANT_QNameA = 0x0D
	val CONSTANT_RTQName = 0x0F
	val CONSTANT_RTQNameA = 0x10
	val CONSTANT_RTQNameL = 0x11
	val CONSTANT_RTQNameLA = 0x12
	val CONSTANT_Multiname = 0x09
	val CONSTANT_MultinameA = 0x0E
	val CONSTANT_MultinameL = 0x1B
	val CONSTANT_MultinameLA = 0x1C
}

object SwfTagTypes {
	val FileAttributes = 69
	val Metadata = 77
	val SetBackgroundColor = 9
	val EnableDebugger2 = 64
	val ScriptLimits = 65
	val DoABC2 = 82
	val SymbolClass = 76
	val ShowFrame = 1
	val End = 0
}

enum class CompressionType {
	NONE,
	ZLIB,
	LZMA
}
