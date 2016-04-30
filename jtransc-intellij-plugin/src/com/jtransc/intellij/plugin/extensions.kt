package com.jtransc.intellij.plugin

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile

// http://www.jetbrains.org/intellij/sdk/docs/basics/project_structure.html

val Module.rootManager: ModuleRootManager get() = com.intellij.openapi.roots.ModuleRootManager.getInstance(this)
val Project.moduleManager: ModuleManager get() = ModuleManager.getInstance(this)
val Project.rootManager: ProjectRootManager get() = ProjectRootManager.getInstance(this)
val Project.sdk: Sdk? get() = this.rootManager.projectSdk

fun Sdk?.getAllClassRoots(): List<VirtualFile> = this?.rootProvider?.getFiles(OrderRootType.CLASSES)?.toList() ?: listOf()

// http://www.jetbrains.org/intellij/sdk/docs/basics/project_structure.html#how-do-i-get-dependencies-and-classpath-of-a-module
fun Module.getAllClassRoots(): Array<out VirtualFile> = this.rootManager.orderEntries().classes().roots

fun Module.getAllClassRootsWithoutSdk(): List<VirtualFile> {
	val sdkRoots = this.project.sdk.getAllClassRoots().toSet()
	return this.rootManager.orderEntries().classes().roots.filter { it !in sdkRoots }
}