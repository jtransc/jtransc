package javax.swing.text;

import javax.accessibility.*;
import javax.print.PrintService;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.Transient;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;

//public abstract class JTextComponent extends JComponent implements Scrollable, Accessible{
public abstract class JTextComponent extends JComponent {
	public JTextComponent() {
		super();
	}

	native public TextUI getUI();

	native public void setUI(TextUI ui);

	native public void addCaretListener(CaretListener listener);

	native public void removeCaretListener(CaretListener listener);

	native public CaretListener[] getCaretListeners();

	native protected void fireCaretUpdate(CaretEvent e);

	native public void setDocument(Document doc);

	native public Document getDocument();

	native public void setComponentOrientation(ComponentOrientation o);

	native public Action[] getActions();

	native public void setMargin(Insets m);

	native public Insets getMargin();

	native public void setNavigationFilter(NavigationFilter filter);

	native public NavigationFilter getNavigationFilter();

	native public Caret getCaret();

	native public void setCaret(Caret c);

	native public Highlighter getHighlighter();

	native public void setHighlighter(Highlighter h);

	native public void setKeymap(Keymap map);

	native public void setDragEnabled(boolean b);

	native public boolean getDragEnabled();

	native public final void setDropMode(DropMode dropMode);

	native public final DropMode getDropMode();

	native public final JTextComponent.DropLocation getDropLocation();

	native public Keymap getKeymap();

	native public static Keymap addKeymap(String nm, Keymap parent);

	native public static Keymap removeKeymap(String nm);

	native public static Keymap getKeymap(String nm);

	public static class KeyBinding {
		public KeyStroke key;
		public String actionName;

		public KeyBinding(KeyStroke key, String actionName) {
			this.key = key;
			this.actionName = actionName;
		}
	}

	native public static void loadKeymap(Keymap map, KeyBinding[] bindings, Action[] actions);

	native public Color getCaretColor();

	native public void setCaretColor(Color c);

	native public Color getSelectionColor();

	native public void setSelectionColor(Color c);

	native public Color getSelectedTextColor();

	native public void setSelectedTextColor(Color c);

	native public Color getDisabledTextColor();

	native public void setDisabledTextColor(Color c);

	native public void replaceSelection(String content);

	native public String getText(int offs, int len) throws BadLocationException;

	native public Rectangle modelToView(int pos) throws BadLocationException;

	native public int viewToModel(Point pt);

	native public void cut();

	native public void copy();

	native public void paste();

	native public void moveCaretPosition(int pos);

	public static final String FOCUS_ACCELERATOR_KEY = "focusAcceleratorKey";

	native public void setFocusAccelerator(char aKey);

	native public char getFocusAccelerator();

	native public void read(Reader in, Object desc) throws IOException;

	native public void write(Writer out) throws IOException;

	native public void removeNotify();

	native public void setCaretPosition(int position);

	@Transient
	native public int getCaretPosition();

	native public void setText(String t);

	native public String getText();

	native public String getSelectedText();

	native public boolean isEditable();

	native public void setEditable(boolean b);

	@Transient
	native public int getSelectionStart();

	native public void setSelectionStart(int selectionStart);

	@Transient
	native public int getSelectionEnd();

	native public void setSelectionEnd(int selectionEnd);

	native public void select(int selectionStart, int selectionEnd);

	native public void selectAll();

	native public String getToolTipText(MouseEvent event);

	native public Dimension getPreferredScrollableViewportSize();

	native public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction);

	native public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction);

	native public boolean getScrollableTracksViewportWidth();

	native public boolean getScrollableTracksViewportHeight();

	native public boolean print() throws PrinterException;

	native public boolean print(final MessageFormat headerFormat, final MessageFormat footerFormat) throws PrinterException;

	native public boolean print(final MessageFormat headerFormat,
								final MessageFormat footerFormat,
								final boolean showPrintDialog,
								final PrintService service,
								final PrintRequestAttributeSet attributes,
								final boolean interactive)
		throws PrinterException;

	native public Printable getPrintable(final MessageFormat headerFormat,
										 final MessageFormat footerFormat);

	native public AccessibleContext getAccessibleContext();

	public class AccessibleJTextComponent extends AccessibleJComponent
		implements AccessibleText, CaretListener, DocumentListener,
		AccessibleAction, AccessibleEditableText,
		AccessibleExtendedText {

		public AccessibleJTextComponent() {
		}

		native public void caretUpdate(CaretEvent e);

		native public void insertUpdate(DocumentEvent e);

		native public void removeUpdate(DocumentEvent e);

		native public void changedUpdate(DocumentEvent e);

		native public AccessibleStateSet getAccessibleStateSet();

		native public AccessibleRole getAccessibleRole();

		native public AccessibleText getAccessibleText();

		native public int getIndexAtPoint(Point p);

		native public Rectangle getCharacterBounds(int i);

		native public int getCharCount();

		native public int getCaretPosition();

		native public AttributeSet getCharacterAttribute(int i);

		native public int getSelectionStart();

		native public int getSelectionEnd();

		native public String getSelectedText();

		native public String getAtIndex(int part, int index);

		native public String getAfterIndex(int part, int index);

		native public String getBeforeIndex(int part, int index);

		native public AccessibleEditableText getAccessibleEditableText();

		native public void setTextContents(String s);

		native public void insertTextAtIndex(int index, String s);

		native public String getTextRange(int startIndex, int endIndex);

		native public void delete(int startIndex, int endIndex);

		native public void cut(int startIndex, int endIndex);

		native public void paste(int startIndex);

		native public void replaceText(int startIndex, int endIndex, String s);

		native public void selectText(int startIndex, int endIndex);

		native public void setAttributes(int startIndex, int endIndex, AttributeSet as);

		native public AccessibleTextSequence getTextSequenceAt(int part, int index);

		native public AccessibleTextSequence getTextSequenceAfter(int part, int index);

		native public AccessibleTextSequence getTextSequenceBefore(int part, int index);

		native public Rectangle getTextBounds(int startIndex, int endIndex);

		native public AccessibleAction getAccessibleAction();

		native public int getAccessibleActionCount();

		native public String getAccessibleActionDescription(int i);

		native public boolean doAccessibleAction(int i);
	}


	public static final class DropLocation extends TransferHandler.DropLocation {
		protected DropLocation() {
			super(new Point());
		}

		native public int getIndex();

		native public Position.Bias getBias();
	}

	public static final String DEFAULT_KEYMAP = "default";

	native protected boolean saveComposedText(int pos);

	native protected void restoreComposedText();
}