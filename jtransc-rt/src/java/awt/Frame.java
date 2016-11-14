package java.awt;

import com.jtransc.widgets.JTranscWidgets;

public class Frame extends Window {
	protected JTranscWidgets.Widget createJTranscWidget() {
		return JTranscWidgets.impl.createComponent("frame");
	}
}
