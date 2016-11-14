package java.awt.event;

import java.awt.*;

public class ActionEvent extends AWTEvent {
	public static final int SHIFT_MASK = Event.SHIFT_MASK;
	public static final int CTRL_MASK = Event.CTRL_MASK;
	public static final int META_MASK = Event.META_MASK;
	public static final int ALT_MASK = Event.ALT_MASK;
	public static final int ACTION_FIRST = 1001;
	public static final int ACTION_LAST = 1001;
	public static final int ACTION_PERFORMED = ACTION_FIRST; //Event.ACTION_EVENT
	String actionCommand;
	long when;
	int modifiers;

	public ActionEvent(Object source, int id, String command) {
		this(source, id, command, 0);
	}

	public ActionEvent(Object source, int id, String command, int modifiers) {
		this(source, id, command, 0, modifiers);
	}

	public ActionEvent(Object source, int id, String command, long when,
					   int modifiers) {
		super(source, id);
		this.actionCommand = command;
		this.when = when;
		this.modifiers = modifiers;
	}

	public String getActionCommand() {
		return actionCommand;
	}

	public long getWhen() {
		return when;
	}

	public int getModifiers() {
		return modifiers;
	}
}