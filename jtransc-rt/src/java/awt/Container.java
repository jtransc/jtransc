package java.awt;

import com.jtransc.widgets.JTranscWidgets;

import java.util.ArrayList;

public class Container extends Component {
	protected JTranscWidgets.Widget createJTranscWidget() {
		return JTranscWidgets.impl.createComponent("container");
	}

	private ArrayList<Component> children = new ArrayList<>();

	public Component add(Component comp) {
		comp.widget.setParent(this.widget);
		children.add(comp);
		return comp;
	}

	public void paintAll(Graphics g) {
		paint(g);
		for (Component child : children) {
			child.paintAll(g);
		}
	}
}
