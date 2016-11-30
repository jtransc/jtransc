package java.awt;

import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.util.ResourceBundle;

public class Window extends Container {
	public enum Type {NORMAL, UTILITY, POPUP}

	//Window() throws HeadlessException {
	Window() {

	}

	public Window(Frame owner) {

	}

	public Window(Window owner) {

	}

	public Window(Window owner, GraphicsConfiguration gc) {

	}

	native public java.util.List<Image> getIconImages();

	public synchronized void setIconImages(java.util.List<? extends Image> icons) {
	}

	public void setIconImage(Image image) {

	}

	public void pack() {
	}

	public void dispose() {
	}

	public Window getOwner() {
		return null;
	}

	public Window[] getOwnedWindows() {
		return new Window[0];
	}

	native public void addWindowListener(WindowListener l);

	native public synchronized void addWindowStateListener(WindowStateListener l);

	native public synchronized void addWindowFocusListener(WindowFocusListener l);

	native public synchronized void removeWindowListener(WindowListener l);

	native public synchronized void removeWindowStateListener(WindowStateListener l);

	native public synchronized void removeWindowFocusListener(WindowFocusListener l);

	native public synchronized WindowListener[] getWindowListeners();

	native public synchronized WindowFocusListener[] getWindowFocusListeners();

	native public synchronized WindowStateListener[] getWindowStateListeners();

	native public final void setAlwaysOnTop(boolean alwaysOnTop) throws SecurityException;

	native public boolean isAlwaysOnTopSupported();

	native public final boolean isAlwaysOnTop();

	native public Component getFocusOwner();

	native public Component getMostRecentFocusOwner();

	native public boolean isActive();

	native public boolean isFocused();

	native public final boolean isFocusableWindow();

	native public boolean getFocusableWindowState();

	native public void setFocusableWindowState(boolean focusableWindowState);

	native public void setAutoRequestFocus(boolean autoRequestFocus);

	native public boolean isAutoRequestFocus();

	native public void applyResourceBundle(ResourceBundle rb);

	native public void applyResourceBundle(String rbName);

	native public void toBack();

	public final String getWarningString() {
		return "";
	}

	native public static Window[] getWindows();

	native public static Window[] getOwnerlessWindows();

	native public void setType(Type type);

	native public Type getType();

	native public void setLocationRelativeTo(Component c);

	native public void setLocationByPlatform(boolean locationByPlatform);

	native public boolean isLocationByPlatform();

	native public float getOpacity();

	native public void setOpacity(float opacity);

	native public Shape getShape();

	native public void setShape(Shape shape);

	//native public void setModalExclusionType(Dialog.ModalExclusionType exclusionType);
	//native public Dialog.ModalExclusionType getModalExclusionType();
}
