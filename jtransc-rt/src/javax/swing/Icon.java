package javax.swing;

import java.awt.*;

public interface Icon {
	void paintIcon(Component c, Graphics g, int x, int y);

	int getIconWidth();

	int getIconHeight();
}
