package java.awt;

import java.awt.event.AWTEventListener;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Map;

public class Toolkit {
	static private Toolkit toolkit = null;

	public static synchronized Toolkit getDefaultToolkit() {
		if (toolkit == null) toolkit = new Toolkit();
		return toolkit;
	}

	public Image createImage(ImageProducer producer) {
		throw new RuntimeException("Not implemented");
	}

	public Image createImage(byte[] imagedata) {
		throw new RuntimeException("Not implemented");
	}

	native public void setDynamicLayout(boolean var1) throws HeadlessException;

	native public boolean isDynamicLayoutActive() throws HeadlessException;

	public native Dimension getScreenSize() throws HeadlessException;

	public native int getScreenResolution() throws HeadlessException;

	native public Insets getScreenInsets(GraphicsConfiguration var1) throws HeadlessException;

	public native ColorModel getColorModel() throws HeadlessException;

	/**
	 * @deprecated
	 */
	@Deprecated
	public native String[] getFontList();

	@Deprecated
	public native FontMetrics getFontMetrics(Font var1);

	public native void sync();

	public native Image getImage(String var1);

	public native Image getImage(URL var1);

	public native Image createImage(String var1);

	public native Image createImage(URL var1);

	public native boolean prepareImage(Image var1, int var2, int var3, ImageObserver var4);

	public native int checkImage(Image var1, int var2, int var3, ImageObserver var4);

	public native Image createImage(byte[] var1, int var2, int var3);

	//native public PrintJob getPrintJob(Frame var1, String var2, Properties var3);
	//native public PrintJob getPrintJob(Frame var1, String var2, JobAttributes var3, PageAttributes var4);
	public native void beep();

	//public native Clipboard getSystemClipboard() throws HeadlessException;
	//native public Clipboard getSystemSelection() throws HeadlessException;
	native public int getMenuShortcutKeyMask() throws HeadlessException;

	native public boolean getLockingKeyState(int var1) throws UnsupportedOperationException;

	native public void setLockingKeyState(int var1, boolean var2) throws UnsupportedOperationException;

	native public Cursor createCustomCursor(Image var1, Point var2, String var3) throws IndexOutOfBoundsException, HeadlessException;

	native public Dimension getBestCursorSize(int var1, int var2) throws HeadlessException;

	native public int getMaximumCursorColors() throws HeadlessException;

	native public boolean isFrameStateSupported(int var1) throws HeadlessException;

	native public static String getProperty(String var0, String var1);

	native public final EventQueue getSystemEventQueue();

	//public native DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent var1) throws InvalidDnDOperationException;

	//native public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> var1, DragSource var2, Component var3, int var4, DragGestureListener var5);

	native public final synchronized Object getDesktopProperty(String var1);

	native public void addPropertyChangeListener(String var1, PropertyChangeListener var2);

	native public void removePropertyChangeListener(String var1, PropertyChangeListener var2);

	native public PropertyChangeListener[] getPropertyChangeListeners();

	native public PropertyChangeListener[] getPropertyChangeListeners(String var1);

	public boolean isAlwaysOnTopSupported() {
		return true;
	}

	public native boolean isModalityTypeSupported(Dialog.ModalityType var1);

	public native boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType var1);

	native public void addAWTEventListener(AWTEventListener var1, long var2);

	native public void removeAWTEventListener(AWTEventListener var1);

	native public AWTEventListener[] getAWTEventListeners();

	native public AWTEventListener[] getAWTEventListeners(long var1);

	public native Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight var1) throws HeadlessException;

	native public boolean areExtraMouseButtonsEnabled() throws HeadlessException;
}
