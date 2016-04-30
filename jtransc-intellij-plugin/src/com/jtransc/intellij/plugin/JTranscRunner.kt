package com.jtransc.intellij.plugin

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.runners.DefaultProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.xdebugger.*
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.*
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.jtransc.AllBuild
import com.jtransc.JTranscVersion
import com.jtransc.ast.AstBuildSettings
import com.jtransc.debugger.JTranscDebugger
import com.jtransc.debugger.v8.NodeJS
import com.jtransc.gen.haxe.HaxeGenDescriptor
import com.jtransc.gen.haxe.HaxeGenTargetProcessor
import com.jtransc.io.ProcessUtils
import java.io.File
import java.io.IOException

// https://github.com/JetBrains/intellij-haxe/blob/master/src/14/com/intellij/plugins/haxe/runner/debugger/HaxeDebugRunner.java
// http://www.jetbrains.org/intellij/sdk/docs/basics/project_structure.html
class JTranscRunner : DefaultProgramRunner() {
	override fun canRun(name: String, profile: RunProfile): Boolean {
		return profile is JTranscRunConfiguration
	}

	override fun getRunnerId(): String {
		return "jtranscRunner"
	}

	override fun doExecute(state: RunProfileState, env: ExecutionEnvironment): RunContentDescriptor? {
		val project = env.project
		val projectRootManager = project.rootManager
		val moduleManager = project.moduleManager
		//val moduleManager = project.getComponent(ModuleManager::class.java)
		val profile = env.runProfile as JTranscRunConfiguration
		//val data = profile.data
		//println("Executing... " + data.mainClass)

		val module = profile.configurationModule.module!!
		val moduleRootManager = module.rootManager

		project.console.print("started!\n", ConsoleViewContentType.NORMAL_OUTPUT)

		println("MAIN_CLASS_NAME: ${profile.MAIN_CLASS_NAME}")
		for (root in module.getAllClassRootsWithoutSdk()) {
			root.canonicalPath
			println("ROOT: $root")
		}

		val outputPath = module.getOutputDirectory()?.canonicalPath ?: "."

		println("outputPath: $outputPath")

		val outputFile = """$outputPath/testintellijplugin.js"""

		val build = AllBuild(
			HaxeGenDescriptor,
			classPaths = module.getAllClassRootsWithoutSdk().map { it.canonicalPath }.filterNotNull(),
			entryPoint = profile.MAIN_CLASS_NAME,
			output = outputFile,
			targetDirectory = "$outputPath",
			subtarget = "js"
		)
		val result = build.buildWithoutRunning(AstBuildSettings(
			jtranscVersion = JTranscVersion.getVersion()
		))
		println(result)



		val debugSession = XDebuggerManager.getInstance(project).startSession(env, object : XDebugProcessStarter() {
			override fun start(session: XDebugSession): XDebugProcess {
				try {
					val debugProcess = JTranscDebugProcess(session, File(outputFile))
					debugProcess.start()
					return debugProcess
				} catch (e: IOException) {
					throw ExecutionException(e.message, e);
				}
			}
		});

		return debugSession.runContentDescriptor
	}

	override fun execute(environment: ExecutionEnvironment, callback: ProgramRunner.Callback?, state: RunProfileState) {
		super.execute(environment, callback, state)
	}
}

class JTranscDebugProcess(session: XDebugSession, val file: File) : XDebugProcess(session) {
	val process = this
	val project = session.project
	val debugger = NodeJS.debug2Async(file, object : ProcessUtils.ProcessHandler() {
		override fun onStarted() {
			println("Started!")
		}

		override fun onOutputData(data: String) {
			intellijWriteAction {
				project.console.isOutputPaused = false
				project.console.print(data, ConsoleViewContentType.NORMAL_OUTPUT)
				System.out.println(data)
			}
		}

		override fun onErrorData(data: String) {
			intellijWriteAction {
				project.console.isOutputPaused = false
				project.console.print(data, ConsoleViewContentType.ERROR_OUTPUT)
				System.err.println(data)
			}
		}

		override fun onCompleted(exitValue: Int) {
			println("EXIT:$exitValue!")
		}
	},  object : JTranscDebugger.EventHandler() {
		override fun onBreak() {
			//println(debugger.currentPosition)
			//println("break!")
			session.positionReached(JTranscSuspendContext(process))
		}
	})

	fun start() {
		println("started!")
	}

	override fun getEditorsProvider(): XDebuggerEditorsProvider {
		return object : XDebuggerEditorsProvider() {
			override fun createDocument(project: Project, text: String, sourcePosition: XSourcePosition?, mode: EvaluationMode): Document {
				throw UnsupportedOperationException()
			}

			override fun getFileType(): FileType {
				return JavaFileType.INSTANCE
			}
		}
	}

	override fun startStepInto() {
		debugger.stepInto()
	}

	override fun startStepOver() {
		debugger.stepOver()
	}

	override fun startStepOut() {
		debugger.stepOut()
	}

	override fun stop() {
		session.stop()
	}

	override fun resume() {
		debugger.resume()
	}

	override fun startPausing() {
		//session.isPaused = true
		//this.expectOK(debugger.Command.BreakNow);
		debugger.pause()
	}

	override fun runToPosition(p0: XSourcePosition) {
		session.positionReached(JTranscSuspendContext(process))
	}

	class JTranscSuspendContext(val process: JTranscDebugProcess) : XSuspendContext() {
		val mainThread = JTranscExecutionStack(process)
		override fun getExecutionStacks(): Array<out XExecutionStack>? {
			return arrayOf(mainThread)
		}

		override fun getActiveExecutionStack(): XExecutionStack? {
			return mainThread
		}
	}

	class JTranscExecutionStack(val process: JTranscDebugProcess) : XExecutionStack("Main Thread", AllIcons.Debugger.ThreadCurrent) {
		val session = process.session
		val frames = listOf(JTranscStackFrame(process), JTranscStackFrame(process), JTranscStackFrame(process))

		override fun getTopFrame(): XStackFrame? {
			//throw UnsupportedOperationException()
			return frames.first()
		}

		override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer) {
			container.addStackFrames(frames, true)
			//throw UnsupportedOperationException()
		}
	}

	class JTranscStackFrame(val process: JTranscDebugProcess) : XStackFrame() {
		override fun computeChildren(node: XCompositeNode) {
			val list = XValueChildrenList()
			list.add("test1", JTranscValue(process))
			list.add("test2", JTranscValue(process))
			node.addChildren(list, true)
		}

		override fun getSourcePosition(): XSourcePosition? {
			//val file = BinaryLightVirtualFile("Test.java", JavaFileType.INSTANCE, "Hello\nWorld\nThis\nIs\nA\nTest".toByteArray())
			val file = process.project.baseDir.findChild("src")?.findChild("Test.java")
			return XSourcePositionImpl.create(file, 6)
			//return null
		}

		override fun getEvaluator(): XDebuggerEvaluator? {
			return super.getEvaluator()
		}
	}

	class JTranscValue(val process: JTranscDebugProcess) : XValue() {
		override fun computePresentation(node: XValueNode, place: XValuePlace) {
			node.setPresentation(AllIcons.Nodes.Property, "type", "value", false)
		}

		override fun computeChildren(node: XCompositeNode) {
			super.computeChildren(node)
		}
	}
}

