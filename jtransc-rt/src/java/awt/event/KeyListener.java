package java.awt.event;

import java.util.EventListener;

public interface KeyListener extends EventListener {
	public void keyTyped(KeyEvent e);

	public void keyPressed(KeyEvent e);

	public void keyReleased(KeyEvent e);
}
