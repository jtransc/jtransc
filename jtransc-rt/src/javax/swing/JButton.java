package javax.swing;

import com.jtransc.widgets.JTranscWidgets;

public class JButton extends AbstractButton {
	protected JTranscWidgets.Widget createJTranscWidget() {
		return JTranscWidgets.impl.createComponent("button");
	}

	public JButton() {
	}

	public JButton(String text) {
		widget.setText(text);
	}
}
