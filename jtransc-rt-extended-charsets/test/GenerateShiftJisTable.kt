import com.jtransc.text.substr
import com.jtransc.text.uescape
import com.jtransc.text.uquote
import com.jtransc.vfs.ResourcesVfs
import org.junit.Ignore
import org.junit.Test

class GenerateShiftJisTable {
	@Test
	@Ignore
	fun generateShiftJIS() {
		val shift = ResourcesVfs(GenerateShiftJisTable::class.java)["SHIFTJIS.TXT"].readString()
		val validLines = Regex("^(\\w+)\\s+(\\w+)")
		var sjisList = ""
		var unicodeList = ""
		for (line in shift.lineSequence()) {
			val info = validLines.find(line) ?: continue
			val sjis = info.groupValues[1].parseInt2()
			val unicode = info.groupValues[2].parseInt2()
			sjisList += sjis.toChar()
			unicodeList += unicode.toChar()
		}
		println(sjisList.uquote())
		println(unicodeList.uquote())
	}

	private fun String.parseInt2(): Int {
		if (this.startsWith("0x", ignoreCase = true)) {
			return Integer.parseInt(this.substr(2), 16)
		} else {
			return Integer.parseInt(this)
		}
	}
}