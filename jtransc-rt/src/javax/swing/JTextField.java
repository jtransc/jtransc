package javax.swing;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

public class JTextField extends JTextComponent implements SwingConstants {
	public static final String notifyAction = "notify-field-accept";

	public JTextField() {
		this(null, null, 0);
	}

	public JTextField(String text) {
		this(null, text, 0);
	}

	public JTextField(int columns) {
		this(null, null, columns);
	}

	public JTextField(String text, int columns) {
		this(null, text, columns);
	}

	public JTextField(Document doc, String text, int columns) {
	}

	native public int getHorizontalAlignment();

	native public void setHorizontalAlignment(int alignment);

	native protected Document createDefaultModel();

	native public int getColumns();

	native public void setColumns(int columns);

	native protected int getColumnWidth();

	native public synchronized void addActionListener(ActionListener l);

	native public synchronized void removeActionListener(ActionListener l);

	native public synchronized ActionListener[] getActionListeners();

	native protected void fireActionPerformed();

	native public void setActionCommand(String command);

	native public void setAction(Action a);

	native public Action getAction();

	native protected void configurePropertiesFromAction(Action a);

	native protected void actionPropertyChanged(Action action, String propertyName);

	native protected PropertyChangeListener createActionPropertyChangeListener(Action a);

	native public void postActionEvent();

	native public BoundedRangeModel getHorizontalVisibility();

	native public int getScrollOffset();

	native public void setScrollOffset(int scrollOffset);
}