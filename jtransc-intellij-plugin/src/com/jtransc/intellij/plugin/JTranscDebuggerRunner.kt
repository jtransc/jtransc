package com.jtransc.intellij.plugin

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.runners.DefaultProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.BinaryLightVirtualFile
import com.intellij.xdebugger.*
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.*
import com.intellij.xdebugger.impl.XSourcePositionImpl
import com.jtransc.AllBuild
import com.jtransc.JTranscVersion
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstTypes
import com.jtransc.debugger.JTranscDebugger
import com.jtransc.debugger.v8.NodeJS
import com.jtransc.gen.haxe.HaxeTarget
import com.jtransc.io.ProcessUtils
import com.jtransc.io.ProcessHandler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream

// https://github.com/JetBrains/intellij-haxe/blob/master/src/14/com/intellij/plugins/haxe/runner/debugger/HaxeDebugRunner.java
// http://www.jetbrains.org/intellij/sdk/docs/basics/project_structure.html
class JTranscDebuggerRunner : DefaultProgramRunner() {
	override fun canRun(name: String, profile: RunProfile): Boolean {
		return profile is JTranscRunConfiguration
	}

	override fun getRunnerId(): String {
		return "jtranscRunner"
	}

	override fun doExecute(state: RunProfileState, env: ExecutionEnvironment): RunContentDescriptor? {
		val programRunner = this
		val project = env.project
		val projectRootManager = project.rootManager
		val moduleManager = project.moduleManager
		//val moduleManager = project.getComponent(ModuleManager::class.java)
		val profile = env.runProfile as JTranscRunConfiguration
		//val data = profile.data
		//println("Executing... " + data.mainClass)
		val executor = env.executor

		val module = profile.configurationModule.module!!
		val moduleRootManager = module.rootManager

		project.console.print("started!\n", ConsoleViewContentType.NORMAL_OUTPUT)

		println("MAIN_CLASS_NAME: ${profile.MAIN_CLASS_NAME}")
		for (root in module.getAllClassRootsWithoutSdk()) {
			root.canonicalPath
			println("ROOT: $root")
		}

		val outputPath = module.getOutputDirectory()?.canonicalPath ?: "."

		//state.execute().processHandler.notifyTextAvailable()

		println("outputPath: $outputPath")
		val consoleComponent = project.console.component // Force show?
		project.console.print("outputPath: $outputPath", ConsoleViewContentType.SYSTEM_OUTPUT)

		//project.editor.showHint("HELLO")
		//HintManager.getInstance().showErrorHint(project.editor, "HELLO")

		val outputFile = """$outputPath/testintellijplugin.js"""

		BuildJTranscRunningState(env, {
			val build = AllBuild(
				HaxeTarget,
				classPaths = module.getAllClassRootsWithoutSdk().map { it.canonicalPath }.filterNotNull(),
				entryPoint = profile.MAIN_CLASS_NAME,
				output = outputFile,
				targetDirectory = "$outputPath",
				subtarget = "js",
				settings = AstBuildSettings(
					jtranscVersion = JTranscVersion.getVersion()
				),
				types = AstTypes()
			)
			val result = build.buildWithoutRunning()
			println(result)
		}).execute(executor, programRunner)

		val debugSession = XDebuggerManager.getInstance(project).startSession(env, object : XDebugProcessStarter() {
			override fun start(session: XDebugSession): XDebugProcess {
				try {
					val debugProcess = JTranscDebugProcess(session, File(outputFile), SimpleJTranscRunningState(env).execute(executor, programRunner))

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

class TaskProcessHandler(action: () -> Unit) : ProcessHandler() {
	init {
		Thread {
			try {
				action()
				notifyProcessTerminated(0)
			} catch (e: Throwable) {
				e.printStackTrace()
				notifyProcessTerminated(-1)
			}
		}.start()
	}

	override fun getProcessInput(): OutputStream? {
		return null
	}

	override fun detachIsDefault(): Boolean {
		return true
	}

	override fun detachProcessImpl() {
	}

	override fun destroyProcessImpl() {
	}
}

class BuildJTranscRunningState(environment: ExecutionEnvironment, val action: () -> Unit) : CommandLineState(environment) {
	override fun startProcess(): ProcessHandler {
		return TaskProcessHandler(action)
	}

}

class SimpleJTranscRunningState(environment: ExecutionEnvironment) : CommandLineState(environment) {
	override fun startProcess(): ProcessHandler {
		//val pb = ProcessBuilder("dir")
		//val process = pb.start()
		//return ColoredProcessHandler(process, pb.command().joinToString(" "))
		return object : ProcessHandler() {
			override fun getProcessInput(): OutputStream? {
				return ByteArrayOutputStream()
			}

			override fun detachIsDefault(): Boolean {
				return true
			}

			override fun detachProcessImpl() {
			}

			override fun destroyProcessImpl() {
			}
		}
	}
}

class JTranscDebugProcess(session: XDebugSession, val file: File, val executionResult: ExecutionResult) : XDebugProcess(session) {
	val process = this
	val project = session.project
	var debugger: JTranscDebugger? = null

	init {
		NodeJS.debug2Async(file, object : ProcessHandler() {
			override fun onStarted() {
				println("Started!")
			}

			override fun onOutputData(data: String) {
				executionResult.processHandler.notifyTextAvailable(data, ProcessOutputTypes.STDOUT)
				System.out.println(data)
			}

			override fun onErrorData(data: String) {
				executionResult.processHandler.notifyTextAvailable(data, ProcessOutputTypes.STDERR)
				System.err.println(data)
			}

			override fun onCompleted(exitValue: Int) {
				println("EXIT:$exitValue!")
			}
		}, object : JTranscDebugger.EventHandler() {
			override fun onBreak() {
				//println(debugger.currentPosition)
				//println("break!")

				session.positionReached(JTranscSuspendContext(process))
			}
		}).then {
			debugger = it
		}
	}

	fun start() {
		executionResult.processHandler.notifyTextAvailable("started!\n", ProcessOutputTypes.SYSTEM)
		//executionResult.processHandler.notifyTextAvailable("STDOUT\n", ProcessOutputTypes.STDOUT)
		println("started!")
	}

	//override fun doGetProcessHandler(): ProcessHandler? {
	//	return executionResult.processHandler
	//}

	override fun createConsole(): ExecutionConsole {
		return executionResult.executionConsole
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
		debugger?.stepInto()
	}

	override fun startStepOver() {
		debugger?.stepOver()
	}

	override fun startStepOut() {
		debugger?.stepOut()
	}

	override fun stop() {
		session.stop()
	}

	override fun resume() {
		debugger?.resume()
	}

	override fun startPausing() {
		//session.isPaused = true
		//this.expectOK(debugger.Command.BreakNow);
		debugger?.pause()
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
		val backtrace = process.debugger!!.backtrace()
		val session = process.session
		val frames = backtrace.map { JTranscStackFrame(process, it) }

		override fun getTopFrame(): XStackFrame? {
			//throw UnsupportedOperationException()
			return frames.first()
		}

		override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer) {
			container.addStackFrames(frames, true)
			//throw UnsupportedOperationException()
		}
	}

	class JTranscStackFrame(val process: JTranscDebugProcess, val frame: JTranscDebugger.Frame) : XStackFrame() {
		override fun computeChildren(node: XCompositeNode) {
			val list = XValueChildrenList()
			for (local in frame.locals) {
				list.add(local.name, JTranscValue(process, local.value))
			}
			node.addChildren(list, true)
		}

		override fun getSourcePosition(): XSourcePosition? {
			//val file = BinaryLightVirtualFile("Test.java", JavaFileType.INSTANCE, "Hello\nWorld\nThis\nIs\nA\nTest".toByteArray())
			val position = frame.position
			val file = LocalFileSystem.getInstance().findFileByIoFile(File(position.normalizedFile)) ?: BinaryLightVirtualFile(position.normalizedFile, PlainTextFileType.INSTANCE, "Unknown\nUnknown\n".toByteArray())
			println("$position :: " + file + " : " + position.line)
			return XSourcePositionImpl.create(file, position.line)
		}

		override fun getEvaluator(): XDebuggerEvaluator? {
			return super.getEvaluator()
		}
	}

	class JTranscValue(val process: JTranscDebugProcess, val value: JTranscDebugger.Value) : XValue() {
		override fun computePresentation(node: XValueNode, place: XValuePlace) {
			node.setPresentation(AllIcons.Nodes.Property, value.type, value.value, false)
		}

		override fun computeChildren(node: XCompositeNode) {
			super.computeChildren(node)
		}
	}
}

