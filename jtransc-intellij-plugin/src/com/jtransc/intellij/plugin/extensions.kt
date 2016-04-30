package com.jtransc.intellij.plugin

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleFileIndex
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.vfs.VirtualFile

// http://www.jetbrains.org/intellij/sdk/docs/basics/project_structure.html

val Module.rootManager: ModuleRootManager get() = com.intellij.openapi.roots.ModuleRootManager.getInstance(this)
val Module.fileIndex: ModuleFileIndex get() = this.rootManager.fileIndex

// http://www.jetbrains.org/intellij/sdk/docs/basics/project_structure.html#how-do-i-get-dependencies-and-classpath-of-a-module
fun Module.getAllClassRoots(): Array<out VirtualFile> = this.rootManager.orderEntries().classes().roots

fun Module.getOutputDirectory() = this.rootManager.orderEntries().withoutDepModules().withoutSdk().withoutLibraries().classes().roots.firstOrNull()

fun Module.getAllClassRootsWithoutSdk(): List<VirtualFile> {
	val sdkRoots = this.project.sdk.getAllClassRoots().toSet()
	return this.rootManager.orderEntries().classes().roots.filter { it !in sdkRoots }
}
val Project.moduleManager: ModuleManager get() = ModuleManager.getInstance(this)
val Project.rootManager: ProjectRootManager get() = ProjectRootManager.getInstance(this)
val Project.sdk: Sdk? get() = this.rootManager.projectSdk

fun <T> UserDataHolder.getOrCreateUserData(key: Key<T>, generator: () -> T): T {
	val result = this.getUserData(key)
	if (result != null) return result
	val genResult = generator()
	this.putUserData(key, genResult)
	return genResult
}

val ProjectConsoleViewKey = Key<ConsoleView>("ProjectConsoleView")

val Project.console: ConsoleView get() {
	return this.getOrCreateUserData(ProjectConsoleViewKey) {
		TextConsoleBuilderFactory.getInstance().createBuilder(this).console
	}
}

fun Sdk?.getAllClassRoots(): List<VirtualFile> = this?.rootProvider?.getFiles(OrderRootType.CLASSES)?.toList() ?: listOf()

fun intellijReadAction(action: () -> Unit) {
	ApplicationManager.getApplication().runReadAction {
		action()
	}
}

fun intellijWriteAction(action: () -> Unit) {
	ApplicationManager.getApplication().runWriteAction {
		action()
	}
}

//TextConsoleBuilderFactory.createBuilder(project).getConsole() creates a ConsoleView instance
//ConsoleView.attachToProcess() attaches it to the output of a process.
