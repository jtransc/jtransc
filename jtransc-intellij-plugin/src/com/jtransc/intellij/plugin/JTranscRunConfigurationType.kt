package com.jtransc.intellij.plugin

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class JTranscRunConfigurationType : ConfigurationType {
	override fun getDisplayName() = "JTransc"

	override fun getConfigurationTypeDescription() = "JTransc Transcompiler"

	override fun getIcon() = JTranscIcons.ICON

	override fun getId() = "jtransc"

	override fun getConfigurationFactories(): Array<ConfigurationFactory> {
		return arrayOf(JTranscConfigurationFactory(this))
	}
}

