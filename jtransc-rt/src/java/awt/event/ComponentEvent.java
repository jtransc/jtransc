package java.awt.event;

import java.awt.*;

public class ComponentEvent extends AWTEvent {
	public static final int COMPONENT_FIRST = 100;
	public static final int COMPONENT_LAST = 103;
	public static final int COMPONENT_MOVED = COMPONENT_FIRST;
	public static final int COMPONENT_RESIZED = 1 + COMPONENT_FIRST;
	public static final int COMPONENT_SHOWN = 2 + COMPONENT_FIRST;
	public static final int COMPONENT_HIDDEN = 3 + COMPONENT_FIRST;

	public ComponentEvent(Component component, int id) {
		super(component, id);
	}
}
