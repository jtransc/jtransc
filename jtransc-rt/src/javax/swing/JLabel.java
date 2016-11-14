package javax.swing;

import com.jtransc.widgets.JTranscWidgets;

public class JLabel extends JComponent {
	protected JTranscWidgets.Widget createJTranscWidget() {
		return JTranscWidgets.impl.createComponent("label");
	}

	public JLabel() {
		this("");
	}

	public JLabel(String text) {
		super();
		setText(text);
	}

	public void setText(String text) {
		widget.setText(text);
	}
}