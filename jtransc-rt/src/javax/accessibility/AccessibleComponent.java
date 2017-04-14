package javax.accessibility;

import javax.accessibility.Accessible;
import java.awt.*;
import java.awt.event.FocusListener;

public interface AccessibleComponent {
	public Color getBackground();

	public void setBackground(Color c);

	public Color getForeground();

	public void setForeground(Color c);

	public Cursor getCursor();

	public void setCursor(Cursor cursor);

	public Font getFont();

	public void setFont(Font f);

	public FontMetrics getFontMetrics(Font f);

	public boolean isEnabled();

	public void setEnabled(boolean b);

	public boolean isVisible();

	public void setVisible(boolean b);

	public boolean isShowing();

	public boolean contains(Point p);

	public Point getLocationOnScreen();

	public Point getLocation();

	public void setLocation(Point p);

	public Rectangle getBounds();

	public void setBounds(Rectangle r);

	public Dimension getSize();

	public void setSize(Dimension d);

	public Accessible getAccessibleAt(Point p);

	public boolean isFocusTraversable();

	public void requestFocus();

	public void addFocusListener(FocusListener l);

	public void removeFocusListener(FocusListener l);
}
