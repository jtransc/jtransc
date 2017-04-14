package java.awt.event;

import java.awt.*;

@SuppressWarnings({"PointlessArithmeticExpression", "WeakerAccess"})
public class WindowEvent extends ComponentEvent {
	public static final int WINDOW_FIRST = 200;
	public static final int WINDOW_OPENED = 0 + WINDOW_FIRST;
	public static final int WINDOW_CLOSING = 1 + WINDOW_FIRST;
	public static final int WINDOW_CLOSED = 2 + WINDOW_FIRST;
	public static final int WINDOW_ICONIFIED = 3 + WINDOW_FIRST;
	public static final int WINDOW_DEICONIFIED = 4 + WINDOW_FIRST;
	public static final int WINDOW_ACTIVATED = 5 + WINDOW_FIRST;
	public static final int WINDOW_DEACTIVATED = 6 + WINDOW_FIRST;
	public static final int WINDOW_GAINED_FOCUS = 7 + WINDOW_FIRST;
	public static final int WINDOW_LOST_FOCUS = 8 + WINDOW_FIRST;
	public static final int WINDOW_STATE_CHANGED = 9 + WINDOW_FIRST;
	public static final int WINDOW_LAST = WINDOW_STATE_CHANGED;
	transient Window opposite;

	private int oldState;
	private int newState;

	public WindowEvent(Window source, int id, Window opposite, int oldState, int newState) {
		super(source, id);
		this.opposite = opposite;
		this.oldState = oldState;
		this.newState = newState;
	}

	public WindowEvent(Window source, int id, Window opposite) {
		this(source, id, opposite, 0, 0);
	}

	public WindowEvent(Window source, int id, int oldState, int newState) {
		this(source, id, null, oldState, newState);
	}

	public WindowEvent(Window source, int id) {
		this(source, id, null, 0, 0);
	}

	public Window getWindow() {
		return (source instanceof Window) ? (Window) source : null;
	}

	public Window getOppositeWindow() {
		return opposite;
	}

	public int getOldState() {
		return oldState;
	}

	public int getNewState() {
		return newState;
	}
}
