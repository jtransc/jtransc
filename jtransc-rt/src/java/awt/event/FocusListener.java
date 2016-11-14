package java.awt.event;

import java.util.EventListener;

public interface FocusListener extends EventListener {
	public void focusGained(FocusEvent e);

	public void focusLost(FocusEvent e);
}
