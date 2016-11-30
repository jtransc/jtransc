package java.awt;

import com.jtransc.widgets.JTranscWidgets;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Frame extends Window implements MenuContainer {
	public static final int DEFAULT_CURSOR = Cursor.DEFAULT_CURSOR;
	public static final int CROSSHAIR_CURSOR = Cursor.CROSSHAIR_CURSOR;
	public static final int TEXT_CURSOR = Cursor.TEXT_CURSOR;
	public static final int WAIT_CURSOR = Cursor.WAIT_CURSOR;
	public static final int SW_RESIZE_CURSOR = Cursor.SW_RESIZE_CURSOR;
	public static final int SE_RESIZE_CURSOR = Cursor.SE_RESIZE_CURSOR;
	public static final int NW_RESIZE_CURSOR = Cursor.NW_RESIZE_CURSOR;
	public static final int NE_RESIZE_CURSOR = Cursor.NE_RESIZE_CURSOR;
	public static final int N_RESIZE_CURSOR = Cursor.N_RESIZE_CURSOR;
	public static final int S_RESIZE_CURSOR = Cursor.S_RESIZE_CURSOR;
	public static final int W_RESIZE_CURSOR = Cursor.W_RESIZE_CURSOR;
	public static final int E_RESIZE_CURSOR = Cursor.E_RESIZE_CURSOR;
	public static final int HAND_CURSOR = Cursor.HAND_CURSOR;
	public static final int MOVE_CURSOR = Cursor.MOVE_CURSOR;
	public static final int NORMAL = 0;
	public static final int ICONIFIED = 1;
	public static final int MAXIMIZED_HORIZ = 2;
	public static final int MAXIMIZED_VERT = 4;
	public static final int MAXIMIZED_BOTH = MAXIMIZED_VERT | MAXIMIZED_HORIZ;

	public Frame() {
	}

	public Frame(GraphicsConfiguration gc) {
	}

	public Frame(String title) throws HeadlessException {
	}

	public Frame(String title, GraphicsConfiguration gc) {
	}

	native public String getTitle();

	native public void setTitle(String title);

	native public Image getIconImage();

	native public MenuBar getMenuBar();

	native public void setMenuBar(MenuBar mb);

	native public boolean isResizable();

	native public void setResizable(boolean resizable);

	native public synchronized void setState(int state);

	native public void setExtendedState(int state);

	native public synchronized int getState();

	native public int getExtendedState();

	native public void setMaximizedBounds(Rectangle bounds);

	native public Rectangle getMaximizedBounds();

	native public void setUndecorated(boolean undecorated);

	native public boolean isUndecorated();

	native public void setCursor(int cursorType);

	native public int getCursorType();

	native public static Frame[] getFrames();

	protected JTranscWidgets.Widget createJTranscWidget() {
		return JTranscWidgets.impl.createComponent("frame");
	}
}
