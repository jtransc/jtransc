package com.jtransc.intellij.plugin

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class JTranscConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
	override fun isApplicable(project: Project): Boolean {
		// @TODO: Check if it is java!
		return true
	}

	override fun createTemplateConfiguration(project: Project) = JTranscRunConfiguration(project, this)
}