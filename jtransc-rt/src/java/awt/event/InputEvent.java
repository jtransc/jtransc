package java.awt.event;

import java.awt.*;

public abstract class InputEvent extends ComponentEvent {
	public static final int SHIFT_MASK = Event.SHIFT_MASK;
	public static final int CTRL_MASK = Event.CTRL_MASK;
	public static final int META_MASK = Event.META_MASK;
	public static final int ALT_MASK = Event.ALT_MASK;
	public static final int ALT_GRAPH_MASK = 1 << 5;
	public static final int BUTTON1_MASK = 1 << 4;
	public static final int BUTTON2_MASK = Event.ALT_MASK;
	public static final int BUTTON3_MASK = Event.META_MASK;
	public static final int SHIFT_DOWN_MASK = 1 << 6;
	public static final int CTRL_DOWN_MASK = 1 << 7;
	public static final int META_DOWN_MASK = 1 << 8;
	public static final int ALT_DOWN_MASK = 1 << 9;
	public static final int BUTTON1_DOWN_MASK = 1 << 10;
	public static final int BUTTON2_DOWN_MASK = 1 << 11;
	public static final int BUTTON3_DOWN_MASK = 1 << 12;
	public static final int ALT_GRAPH_DOWN_MASK = 1 << 13;

	public long when;
	public int modifiers;
	public boolean consumed;

	public InputEvent(Component source, int id, long when, int modifiers) {
		super(source, id);
		this.when = when;
		this.modifiers = modifiers;
	}

	public static int getMaskForButton(int button) {
		int offset = 1 << (10 + button);
		if (button < 4) offset--;
		return 1 << offset;
	}

	public boolean isShiftDown() {
		return (modifiers & SHIFT_MASK) != 0;
	}
	public boolean isControlDown() {
		return (modifiers & CTRL_MASK) != 0;
	}
	public boolean isMetaDown() {
		return (modifiers & META_MASK) != 0;
	}
	public boolean isAltDown() {
		return (modifiers & ALT_MASK) != 0;
	}
	public boolean isAltGraphDown() {
		return (modifiers & ALT_GRAPH_MASK) != 0;
	}
	public long getWhen() {
		return when;
	}
	public int getModifiers() {
		return modifiers & ((SHIFT_DOWN_MASK - 1) | (~( (1 << 31) - 1 )));
	}
	public int getModifiersEx() {
		return modifiers & ~(SHIFT_DOWN_MASK - 1);
	}
	public void consume() {
		consumed = true;
	}
	public boolean isConsumed() {
		return consumed;
	}

	public static String getModifiersExText(int modifiers) {
		return "MODS" + modifiers;
	}
}
