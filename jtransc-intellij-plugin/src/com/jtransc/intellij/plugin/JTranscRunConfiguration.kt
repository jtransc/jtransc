package com.jtransc.intellij.plugin

import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.application.ApplicationConfigurable
import com.intellij.execution.application.ApplicationConfiguration
import com.intellij.execution.configuration.AbstractRunConfiguration
import com.intellij.execution.configuration.EmptyRunProfileState
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.Element
import java.io.OutputStream
import javax.swing.JComponent

//class JTranscRunConfiguration(project: Project, configurationFactory: JTranscConfigurationFactory) : AbstractRunConfiguration(project, configurationFactory) {
class JTranscRunConfiguration(project: Project, factory: JTranscConfigurationFactory) : ApplicationConfiguration("jtransc", project, factory) {

	//val LOG = com.intellij.openapi.diagnostic.Logger.getInstance(JTranscRunConfiguration::class.java.name);

	//class Data {
	//	@JvmField var mainClass = "test"
	//}
//
	//val data = Data()

	override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
		//JavaSettings
		//return JTranscSettingsEditor()
		return ApplicationConfigurable(project)
	}

	override fun checkConfiguration() {
		super.checkConfiguration()
		//throw RuntimeConfigurationException("Testing invalid configuration!")
	}

	override fun getState(p0: Executor, p1: ExecutionEnvironment): RunProfileState? {
		//return EmptyRunProfileState.INSTANCE
		return RunProfileState { executor, programRunner ->
			println("executor: $executor")
			println("programRunner: $programRunner")
			object : ExecutionResult {
				override fun getExecutionConsole(): ExecutionConsole? {
					return ConsoleViewImpl(p1.project, true)
				}

				override fun getProcessHandler(): ProcessHandler? {
					return object : ProcessHandler() {
						override fun getProcessInput(): OutputStream? {
							//throw UnsupportedOperationException()
							return null
						}

						override fun detachIsDefault(): Boolean {
							//throw UnsupportedOperationException()
							return true
						}

						override fun detachProcessImpl() {
							//throw UnsupportedOperationException()
						}

						override fun destroyProcessImpl() {
							//throw UnsupportedOperationException()
						}

					}
				}

				override fun getActions(): Array<out AnAction>? {
					return arrayOf()
				}
			}
		}
	}

	override fun getValidModules(): MutableCollection<Module>? {
		return null
	}

	override fun onNewConfigurationCreated() {
		super.onNewConfigurationCreated()
	}


	//override fun writeExternal(element: Element) {
	//	super.writeExternal(element);
	//	writeModule(element);
	//	XmlSerializer.serializeInto(this.data, element);
	//}
//
	//override fun readExternal(element: Element) {
	//	super.readExternal(element);
	//	readModule(element);
	//	XmlSerializer.deserializeInto(this.data, element);
	//}
}

//class JTranscSettingsEditor : SettingsEditor<JTranscRunConfiguration>() {
//	var form = JTranscRunConfigurationEditor()
//
//	override fun resetEditorFrom(p0: JTranscRunConfiguration) {
//		form.mainClassTextfield.text = p0.data.mainClass
//	}
//
//	override fun createEditor(): JComponent {
//		form = JTranscRunConfigurationEditor()
//		return form.contentPanel
//	}
//
//	override fun applyEditorTo(p0: JTranscRunConfiguration) {
//		p0.data.mainClass = form.mainClassTextfield.text
//		//p0.putCopyableUserData(JTranscKeys.mainClass, )
//	}
//}

/*
object JTranscKeys {
	val mainClass: com.intellij.openapi.util.Key<String> = Key("mainClass")
}
*/