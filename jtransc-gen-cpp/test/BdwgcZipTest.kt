import com.jtransc.gen.cpp.libs.BdwgcLib
import com.jtransc.vfs.MemoryVfs
import com.jtransc.vfs.ZipVfs
import com.jtransc.vfs.getResourceBytes
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class BdwgcZipTest {
	@Test
	fun name() {
		val mem = MemoryVfs()
		val gzzip = ZipVfs(BdwgcLib::class.java.classLoader.getResourceBytes("com/jtransc/gen/cpp/libs/bdwgc.zip"))
		gzzip.copyTreeTo(mem)
		Assert.assertEquals(378, mem.listdirRecursive().count())
		Assert.assertEquals(8479, gzzip["bdwgc/CMakeLists.txt"].readString().length)
	}

	@Test
	@Ignore
	fun name2() {
		val jarFile = "C:/Users/soywiz/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/1.1.2-3/552a40eb47669b78f0f194d526cb21b3aa1f8319/kotlin-stdlib-1.1.2-3.jar"
		val zip = ZipVfs(jarFile)
		println(jarFile)
		val data = zip["kotlin/jvm/internal/StringCompanionObject.class"]
		for (file in data.listdirRecursive()) {
			println(file.name)
		}
		println(data.size)
		println(data.read().size)
	}
}