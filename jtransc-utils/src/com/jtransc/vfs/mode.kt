package com.jtransc.vfs

import com.jtransc.numeric.toInt
import com.jtransc.util.extract
import com.jtransc.util.extractBool
import com.jtransc.util.withBool
import java.nio.file.attribute.PosixFilePermission

data class PartPermission(val value: Int) {
	val canExec = value.extractBool(0)
	val canWrite = value.extractBool(1)
	val canRead = value.extractBool(2)

	companion object {
		fun fromStr(str: String): PartPermission {
			var out = 0
			if (str.contains('x')) out = out.withBool(0)
			if (str.contains('w')) out = out.withBool(1)
			if (str.contains('r')) out = out.withBool(2)
			return PartPermission(out)
		}
	}
}

data class FileMode(val value: Int) {
	val other = PartPermission(value.extract(0, 3))
	val group = PartPermission(value.extract(3, 3))
	val owner = PartPermission(value.extract(6, 3))

	companion object {
		fun fromInt(value: Int) = FileMode(value)
		fun fromOctal(str: String) = FileMode(str.toInt(8, 0))
		fun fromParts(owner: PartPermission, group: PartPermission, other: PartPermission): FileMode {
			return FileMode((owner.value shl 6) or (group.value shl 3) or (other.value shl 0))
		}

		fun fromString(str: String): FileMode {
			val other = str.substring(str.length - 3, str.length - 0)
			val group = str.substring(str.length - 6, str.length - 3)
			val owner = str.substring(str.length - 9, str.length - 6)
			return fromParts(PartPermission.fromStr(owner), PartPermission.fromStr(group), PartPermission.fromStr(other))
		}

		val FULL_ACCESS = fromOctal("0777")
	}
}

fun FileMode.toPosix(): Set<PosixFilePermission> {
	// Symbolic Notation	Numeric Notation	English
	// ----------	0000	no permissions
	// ---x--x--x	0111	execute
	// --w--w--w-	0222	write
	// --wx-wx-wx	0333	write & execute
	// -r--r--r--	0444	read
	// -r-xr-xr-x	0555	read & execute
	// -rw-rw-rw-	0666	read & write
	// -rwxrwxrwx	0777	read, write, & execute
	// -rwxr-----	0740	user can read, write, & execute; group can only read; others have no permissions
	val out = hashSetOf<PosixFilePermission>()

	if (this.other.canRead) out += PosixFilePermission.OTHERS_READ
	if (this.other.canWrite) out += PosixFilePermission.OTHERS_WRITE
	if (this.other.canExec) out += PosixFilePermission.OTHERS_EXECUTE

	if (this.group.canRead) out += PosixFilePermission.GROUP_READ
	if (this.group.canWrite) out += PosixFilePermission.GROUP_WRITE
	if (this.group.canExec) out += PosixFilePermission.GROUP_EXECUTE

	if (this.owner.canRead) out += PosixFilePermission.OWNER_READ
	if (this.owner.canWrite) out += PosixFilePermission.OWNER_WRITE
	if (this.owner.canExec) out += PosixFilePermission.OWNER_EXECUTE

	return out
}
