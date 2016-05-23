package com.jtransc.gradle

import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project

// Kotlin extensions
fun <T> Project.getIfExists(name: String): T? = if (this.hasProperty(name)) this.property(name) as T else null

operator fun <T> NamedDomainObjectSet<T>.get(key: String): T = this.getByName(key)