package java.awt.event;

import java.util.EventListener;

public interface WindowListener extends EventListener {
	void windowOpened(WindowEvent e);

	void windowClosing(WindowEvent e);

	void windowClosed(WindowEvent e);

	void windowIconified(WindowEvent e);

	void windowDeiconified(WindowEvent e);

	void windowActivated(WindowEvent e);

	void windowDeactivated(WindowEvent e);
}
