package javax.swing;

import java.awt.*;

@SuppressWarnings("WeakerAccess")
public abstract class LayoutStyle {
	native public static void setInstance(LayoutStyle style);

	native public static LayoutStyle getInstance();

	public enum ComponentPlacement {
		RELATED, UNRELATED, INDENT;
	}

	public LayoutStyle() {
	}

	public abstract int getPreferredGap(JComponent component1, JComponent component2, ComponentPlacement type, int position, Container parent);

	public abstract int getContainerGap(JComponent component, int position, Container parent);
}
