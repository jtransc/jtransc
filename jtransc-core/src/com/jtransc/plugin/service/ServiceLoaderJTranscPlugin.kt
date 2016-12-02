package com.jtransc.plugin.service

import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.gen.TargetName
import com.jtransc.log.log
import com.jtransc.plugin.JTranscPlugin
import com.jtransc.vfs.getUnmergedFiles
import java.util.*

/**
 * Plugin that reference classes in META-INF/services folder for ServiceLoader to work
 */
class ServiceLoaderJTranscPlugin : JTranscPlugin() {
	var alreadyExecuted = false
	val servicesToImpls = hashMapOf<String, List<String>>()
	val referencedServices = arrayListOf<String>()

	override fun onAfterClassDiscovered(clazz: AstType.REF, program: AstProgram) {
		//println("Referenced: $clazz")
	}

	override fun onAfterAllClassDiscovered(program: AstProgram) {
		if (alreadyExecuted) return
		if (ServiceLoader::class.java.fqname !in program) return

		val targetName = program.injector.get<TargetName>()

		alreadyExecuted = true

		servicesToImpls.clear()
		referencedServices.clear()

		//log.info("Referenced ServiceLoader!")

		val servicesFolders = program.resourcesVfs["META-INF/services"].getUnmergedFiles().filter { it.exists && it.isDirectory }
		val targetRegex = Regex("<target=([^>]*)>")
		for (serviceListFile in servicesFolders.flatMap { it.listdir() }) {
			val serviceName = serviceListFile.name
			if (serviceName !in servicesToImpls) servicesToImpls[serviceName] = listOf()
			for (line in serviceListFile.file.readString().trim().lines()) {
				val parts = line.split('#')
				val serviceImpl = parts.getOrNull(0)?.trim() ?: continue
				val comment = parts.getOrNull(1) ?: ""
				val targets = (targetRegex.find(comment)?.groups?.get(1)?.value ?: "").split(',').map { it.trim().toLowerCase() }.distinct().toSet()
				val isForTarget = targetName.matches(targets.toList())
				if (isForTarget) {
					servicesToImpls[serviceName] = servicesToImpls[serviceName]!! + serviceImpl
					log.info("Detected service: $serviceName with implementations $serviceImpl for targets $targets")
				} else {
					log.info("Detected service not included for $targetName: $serviceName with implementations $serviceImpl for targets $targets")
				}
			}
		}

		for (clazz in program.classes) {
			val impls = servicesToImpls[clazz.fqname]
			if (impls != null) {
				referencedServices += clazz.fqname
				//log.info("Discovered used service: $clazz with impls $impls")
				log.info("Discovered used service: $clazz with impls $impls")
				for (impl in impls) {
					program.addReference(AstType.REF(impl.fqname), clazz.ref)
				}
			}
		}
	}

	override fun processBeforeTreeShaking(programBase: AstProgram) {
		if (referencedServices.isEmpty()) return

		val ServiceLoaderClass = programBase[ServiceLoader::class.java.fqname]
		val objectsClass = programBase[Objects::class.java.fqname]
		val ServiceLoader_getInstances = ServiceLoaderClass.getMethodWithoutOverrides("getInstances") ?: return
		val Objects_equals = objectsClass.getMethodWithoutOverrides(Objects::equals.name) ?: return
		ServiceLoader_getInstances.replaceBodyOptBuild {
			val nameArg = AstArgument(0, AstType.STRING)
			val out = AstLocal(0, "out", ARRAY(OBJECT))

			SET(out, NULL)
			for (serviceName in referencedServices) {
				val impls = servicesToImpls[serviceName]!!
				IF(Objects_equals(nameArg.expr, serviceName.lit)) {
					SET(out, NEW_ARRAY(ARRAY(OBJECT), impls.size.lit))
					for ((index, impl) in impls.withIndex()) {
						//val ref = AstType.REF(impl.fqname)
						val clazz = programBase[impl.fqname]
						val emptyConstructor = clazz[AstMethodWithoutClassRef("<init>", AstType.METHOD(AstType.VOID, listOf()))] ?: invalidOp("Can't find default constructor for service implementation $impl")
						//clazz.extraKeep = true
						SET_ARRAY(out, index.lit, AstExpr.NEW_WITH_CONSTRUCTOR(emptyConstructor.ref, listOf()))
					}
					RETURN(out)
				}
			}
			RETURN(NEW_ARRAY(ARRAY(OBJECT), 0.lit))
		}
	}

	override fun processAfterTreeShaking(program: AstProgram) {
		//println(servicesFolders)
	}
}