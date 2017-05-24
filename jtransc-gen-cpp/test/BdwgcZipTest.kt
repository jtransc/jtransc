import com.jtransc.gen.cpp.libs.BdwgcLib
import com.jtransc.vfs.MemoryVfs
import com.jtransc.vfs.ZipVfs
import com.jtransc.vfs.getResourceBytes
import org.junit.Assert
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
}