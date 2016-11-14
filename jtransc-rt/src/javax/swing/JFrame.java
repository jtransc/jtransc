package javax.swing;

import java.awt.*;

public class JFrame extends Frame {
	public static final int EXIT_ON_CLOSE = 3;

	public JFrame(String title) throws HeadlessException {
	}

	public Container getContentPane() {
		return this;
	}

	public void setDefaultCloseOperation(int operation) {
	}
}
