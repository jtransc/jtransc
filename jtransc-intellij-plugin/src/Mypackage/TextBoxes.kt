package Mypackage

import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.util.ui.UIUtil
import com.jtransc.intellij.plugin.console

class TextBoxes : AnAction("Text _Boxes") {

	override fun actionPerformed(event: AnActionEvent) {
		val project = event.getData(PlatformDataKeys.PROJECT)!!
		val txt = Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon())
		val console = project.console
		//project!!.console.
		project!!.console.print("HELLO", ConsoleViewContentType.SYSTEM_OUTPUT)
		console.component.show()
		Messages.showMessageDialog(project, "Hello, $txt!\n I am glad to see you.", "Information", Messages.getInformationIcon())
	}
}