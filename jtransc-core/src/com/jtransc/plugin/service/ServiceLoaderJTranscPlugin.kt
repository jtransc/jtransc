package com.jtransc.plugin.service

import com.jtransc.ast.AstProgram
import com.jtransc.ast.fqname
import com.jtransc.plugin.JTranscPluginAdaptor
import com.jtransc.vfs.getUnmergedFiles
import java.util.*

/**
 * Plugin that reference classes in META-INF/services folder for ServiceLoader to work
 */
class ServiceLoaderJTranscPlugin : JTranscPluginAdaptor() {
	override fun processAfterTreeShaking(program: AstProgram) {
		val resourcesVfs = program.resourcesVfs
		val servicesFolders = program.resourcesVfs["META-INF/services"].getUnmergedFiles().filter { it.exists && it.isDirectory }
		for (serviceListFile in servicesFolders.flatMap { it.listdir() }) {
			val serviceName = serviceListFile.name
			val serviceImpls = serviceListFile.file.readLines()
			println("Detected service: $serviceName with implementations $serviceImpls")
		}

		if (ServiceLoader::class.java.fqname in program) {
			println("Referenced ServiceLoader!")
			val serviceLoaderClass = program[ServiceLoader::class.java.fqname]
			val getInstancesMethod = serviceLoaderClass.getMethodWithoutOverrides("getInstances")
		}
		//println(servicesFolders)
	}
}