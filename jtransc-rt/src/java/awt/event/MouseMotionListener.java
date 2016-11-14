package java.awt.event;

import java.util.EventListener;

public interface MouseMotionListener extends EventListener {
	public void mouseDragged(MouseEvent e);
	public void mouseMoved(MouseEvent e);
}
