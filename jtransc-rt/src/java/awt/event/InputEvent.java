package java.awt.event;

import java.awt.*;

public abstract class InputEvent extends ComponentEvent {
	public long when;
	public int modifiers;

	public InputEvent(Component source, int id, long when, int modifiers) {
		super(source, id);
		this.when = when;
		this.modifiers = modifiers;
	}
}
