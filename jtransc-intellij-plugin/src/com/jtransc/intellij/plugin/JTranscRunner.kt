package com.jtransc.intellij.plugin

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.DefaultProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
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

		val mainModule = profile.configurationModule.module!!

		println("MAIN_CLASS_NAME: ${profile.MAIN_CLASS_NAME}")
		for (root in mainModule.getAllClassRootsWithoutSdk()) {
			println("ROOT: $root")
		}

		val debugSession = XDebuggerManager.getInstance(project).startSession(env, object : XDebugProcessStarter() {
			override fun start(session: XDebugSession): XDebugProcess {
				try {
					// Start the debugger process, which is a class that
					// implements the actual debugger functionality.  In this
					// case, it does so by message passing through a socket.
					val debugProcess = JTranscDebugProcess(session)

					// Now accept the connection from the being-debugged
					// process.
					debugProcess.start()

					return debugProcess;
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

class JTranscDebugProcess(session: XDebugSession) : XDebugProcess(session) {
	fun start() {
		val console = createConsole()
		println("started!")

		//session.consoleView.print("Started debugging!", ConsoleViewContentType.ERROR_OUTPUT)

		session.positionReached(JTranscSuspendContext(session))

		//session.updateBreakpointPresentation(breakpoint, AllIcons.Debugger.Db_invalid_breakpoint, null);
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
		session.positionReached(JTranscSuspendContext(session))
	}

	override fun startStepOver() {
		session.positionReached(JTranscSuspendContext(session))
	}

	override fun startStepOut() {
		session.positionReached(JTranscSuspendContext(session))
	}

	override fun stop() {
		session.stop()
	}

	override fun resume() {
	}

	override fun startPausing() {
		//session.isPaused = true
		//this.expectOK(debugger.Command.BreakNow);
		session.positionReached(JTranscSuspendContext(session))
	}

	override fun runToPosition(p0: XSourcePosition) {
		session.positionReached(JTranscSuspendContext(session))
	}

	class JTranscSuspendContext(val session: XDebugSession) : XSuspendContext() {
		val mainThread = JTranscExecutionStack(session)
		override fun getExecutionStacks(): Array<out XExecutionStack>? {
			return arrayOf(mainThread)
		}

		override fun getActiveExecutionStack(): XExecutionStack? {
			return mainThread
		}
	}

	class JTranscExecutionStack(val session: XDebugSession) : XExecutionStack("Main Thread", AllIcons.Debugger.ThreadCurrent) {
		val frames = listOf(JTranscStackFrame(session), JTranscStackFrame(session), JTranscStackFrame(session))

		override fun getTopFrame(): XStackFrame? {
			//throw UnsupportedOperationException()
			return frames.first()
		}

		override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer) {
			container.addStackFrames(frames, true)
			//throw UnsupportedOperationException()
		}


	}

	class JTranscStackFrame(val session: XDebugSession) : XStackFrame() {
		override fun computeChildren(node: XCompositeNode) {
			val list = XValueChildrenList()
			list.add("test1", JTranscValue(session))
			list.add("test2", JTranscValue(session))
			node.addChildren(list, true)
		}

		override fun getSourcePosition(): XSourcePosition? {
			//val file = BinaryLightVirtualFile("Test.java", JavaFileType.INSTANCE, "Hello\nWorld\nThis\nIs\nA\nTest".toByteArray())
			val file = session.project.baseDir.findChild("src")?.findChild("Test.java")
			return XSourcePositionImpl.create(file, 6)
			//return null
		}

		override fun getEvaluator(): XDebuggerEvaluator? {
			return super.getEvaluator()
		}
	}

	class JTranscValue(val session: XDebugSession) : XValue() {
		override fun computePresentation(node: XValueNode, place: XValuePlace) {
			node.setPresentation(AllIcons.Nodes.Property, "type", "value", false)
		}

		override fun computeChildren(node: XCompositeNode) {
			super.computeChildren(node)
		}
	}
}

