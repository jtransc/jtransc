package java.awt.event;

import java.awt.*;

public class FocusEvent extends ComponentEvent {
	public static final int FOCUS_FIRST = 1004;
	public static final int FOCUS_LAST = 1005;
	public static final int FOCUS_GAINED = FOCUS_FIRST; //Event.GOT_FOCUS
	public static final int FOCUS_LOST = 1 + FOCUS_FIRST; //Event.LOST_FOCUS

	boolean temporary;

	transient Component opposite;

	public FocusEvent(Component source, int id, boolean temporary, Component opposite) {
		super(source, id);
		this.temporary = temporary;
		this.opposite = opposite;
	}

	public FocusEvent(Component source, int id, boolean temporary) {
		this(source, id, temporary, null);
	}

	public FocusEvent(Component source, int id) {
		this(source, id, false);
	}

	public boolean isTemporary() {
		return temporary;
	}

	public Component getOppositeComponent() {
		return opposite;
	}

}
