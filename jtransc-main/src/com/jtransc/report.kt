package com.jtransc

import com.jtransc.maven.MavenLocalRepository
import com.jtransc.vfs.GetClassJar
import com.jtransc.vfs.MergedLocalAndJars
import com.jtransc.vfs.ZipVfs
import jtransc.JTranscVersion

object JTranscRtReport {
	@JvmStatic fun main(args: Array<String>) {
		//val classLoader = JTranscRtReport::class.java.classLoader
		//jar:file:/Library/Java/JavaVirtualMachines/jdk1.8.0_74.jdk/Contents/Home/jre/lib/rt.jar!/java/lang/String.class

		//println(url)
		val jtranscVersion = JTranscVersion.getVersion()
		val javaRt = ZipVfs(GetClassJar(String::class.java))
		val jtranscRt = MergedLocalAndJars(MavenLocalRepository.locateJars("com.jtransc:jtransc-rt:$jtranscVersion"))
		//val orig = javaRt["java/lang/String.class"].readBytes()
		//val jtransc = jtranscRt["java/lang/String.class"].readBytes()
		//println(jtranscRt.listdir())
		//println(jtranscRt["java"].listdir())
		//println(javaRt.listdir())
		/*
		for (e in javaRt.listdir()) {
			println(e)
		}
		for (e in javaRt["com"].listdir()) {
			println(e)
		}
		for (e in javaRt["java/lang"].listdir()) {
			println(e)
		}
		*/
		for (e in javaRt.listdirRecursive().filter { it.name.endsWith(".class") }) {
			println(e.path)
		}
		//for (e in jtranscRt.listdirRecursive().filter { it.name.endsWith(".class") }) {
		//	println(e.path)
		//}
	}
}