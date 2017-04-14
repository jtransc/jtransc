package java.awt.event;

import java.util.EventListener;

public interface ComponentListener extends EventListener {
	public void componentResized(ComponentEvent e);

	public void componentMoved(ComponentEvent e);

	public void componentShown(ComponentEvent e);

	public void componentHidden(ComponentEvent e);
}
