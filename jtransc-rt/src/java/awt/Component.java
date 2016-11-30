package java.awt;

import com.jtransc.widgets.JTranscWidgets;

import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.image.*;
import java.awt.peer.ComponentPeer;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings({"unused", "WeakerAccess", "deprecation"})
public class Component implements ImageObserver, MenuContainer, Serializable {
	protected JTranscWidgets.Widget widget;

	public Component() {
		widget = createJTranscWidget();
		widget.listener = new JTranscWidgets.EventListener() {
			@Override
			public void handle(String kind) {
				switch (kind) {
					case "click":
						int modifiers = 0;
						int x = 0;
						int y = 0;
						int clickCount = 0;
						boolean popupTrigger = false;
						processMouseEvent(new MouseEvent(Component.this, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), modifiers, x, y, clickCount, popupTrigger));
						break;
				}
			}
		};
	}

	protected JTranscWidgets.Widget createJTranscWidget() {
		return JTranscWidgets.impl.createComponent("canvas");
	}

	public static final float TOP_ALIGNMENT = 0.0f;
	public static final float CENTER_ALIGNMENT = 0.5f;
	public static final float BOTTOM_ALIGNMENT = 1.0f;
	public static final float LEFT_ALIGNMENT = 0.0f;
	public static final float RIGHT_ALIGNMENT = 1.0f;

	public enum BaselineResizeBehavior {CONSTANT_ASCENT, CONSTANT_DESCENT, CENTER_OFFSET, OTHER}

	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	Container parent;

	public Container getParent() {
		return parent;
	}

	DropTarget dropTarget = null;

	public synchronized void setDropTarget(DropTarget dt) {
		dropTarget = dt;
	}

	public synchronized DropTarget getDropTarget() {
		return dropTarget;
	}

	public Toolkit getToolkit() {
		return Toolkit.getDefaultToolkit();
	}

	public boolean isValid() {
		return true;
	}

	public ComponentPeer getPeer() {
		return null;
	}

	public GraphicsConfiguration getGraphicsConfiguration() {
		return null;
	}

	public final Object getTreeLock() {
		return this;
	}

	public boolean isDisplayable() {
		return true;
	}

	boolean isRecursivelyVisible() {
		return true;
	}

	public Point getMousePosition() throws HeadlessException {
		return new Point(0, 0);
	}

	public boolean isShowing() {
		return true;
	}

	public boolean isDoubleBuffered() {
		return false;
	}

	public void enableInputMethods(boolean enable) {

	}

	private boolean enabled = true;

	public boolean isEnabled() {
		return true;
	}

	public void setEnabled(boolean b) {
		enabled = b;
		widget.setEnabled(b);
	}

	@Deprecated
	public void enable() {
		setEnabled(true);
	}

	@Deprecated
	public void disable() {
		setEnabled(false);
	}

	@Deprecated
	public void enable(boolean b) {
		setEnabled(b);
	}

	private boolean visible = true;

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean b) {
		visible = b;
		widget.setVisible(b);
	}

	public void show() {
		setVisible(true);
	}

	public void hide() {
		setVisible(false);
	}

	public void show(boolean b) {
		setVisible(b);
	}

	Color foreground = Color.black;
	Color background = Color.gray;

	public Color getForeground() {
		return foreground;
	}

	public void setForeground(Color c) {
		foreground = c;
	}

	public boolean isForegroundSet() {
		return true;
	}

	public Color getBackground() {
		return background;
	}

	public void setBackground(Color c) {
		this.background = c;
	}

	public boolean isBackgroundSet() {
		return true;
	}


	Font font = null;

	public Font getFont() {
		return font;
	}

	public void setFont(Font f) {
		this.font = f;
	}

	public boolean isFontSet() {
		return font != null;
	}

	Locale locale = null;

	public Locale getLocale() {
		if (locale == null) locale = Locale.getDefault();
		return locale;
	}

	public void setLocale(Locale l) {
		this.locale = l;
	}

	public ColorModel getColorModel() {
		return null;
	}

	public Point getLocation() {
		return new Point(getX(), getY());
	}

	public Point getLocationOnScreen() {
		// @TODO: Check parents
		return getLocation();
	}

	@Deprecated
	public Point location() {
		return getLocation();
	}

	public void setLocation(int x, int y) {
	}

	public void move(int x, int y) {
		setLocation(x, y);
	}

	public void setLocation(Point p) {
		setLocation(p.x, p.y);
	}

	public Dimension getSize() {
		return new Dimension(getWidth(), getHeight());
	}

	public Dimension size() {
		return getSize();
	}

	public void setSize(int width, int height) {
	}

	public void resize(int width, int height) {
		setSize(width, height);
	}

	public void setSize(Dimension d) {
		resize(d);
	}

	public void resize(Dimension d) {
		setSize(d.width, d.height);
	}

	public Rectangle getBounds() {
		return getBounds(null);
	}

	public Rectangle bounds() {
		return getBounds(null);
	}

	public void setBounds(int x, int y, int width, int height) {
		reshape(x, y, width, height);
	}

	public void reshape(int x, int y, int width, int height) {
	}

	public void setBounds(Rectangle r) {
		setBounds(r.x, r.y, r.width, r.height);
	}


	public int getX() {
		return 0;
	}

	public int getY() {
		return 0;
	}

	public int getWidth() {
		return 100;
	}

	public int getHeight() {
		return 100;
	}

	public Rectangle getBounds(Rectangle rv) {
		if (rv == null) rv = new Rectangle();
		rv.setBounds(getX(), getY(), getWidth(), getHeight());
		return rv;
	}

	public Dimension getSize(Dimension rv) {
		if (rv == null) rv = new Dimension();
		rv.setSize(getWidth(), getHeight());
		return rv;
	}

	public Point getLocation(Point rv) {
		if (rv == null) rv = new Point();
		rv.setLocation(getWidth(), getHeight());
		return rv;
	}

	public boolean isOpaque() {
		return true;
	}

	public boolean isLightweight() {
		return true;
	}

	private Dimension preferredSize;
	private Dimension minimumSize;
	private Dimension maximumSize;

	public void setPreferredSize(Dimension preferredSize) {
		this.preferredSize = preferredSize;
	}

	public boolean isPreferredSizeSet() {
		return preferredSize != null;
	}

	public Dimension getPreferredSize() {
		return preferredSize;
	}

	public Dimension preferredSize() {
		return getPreferredSize();
	}

	public void setMinimumSize(Dimension minimumSize) {
		this.minimumSize = minimumSize;
	}

	public boolean isMinimumSizeSet() {
		return minimumSize != null;
	}

	public Dimension getMinimumSize() {
		return minimumSize;
	}

	public Dimension minimumSize() {
		return getMinimumSize();
	}

	public void setMaximumSize(Dimension maximumSize) {
		this.maximumSize= maximumSize;
	}

	public boolean isMaximumSizeSet() {
		return maximumSize != null;
	}

	public Dimension getMaximumSize() {
		return maximumSize;
	}

	public float getAlignmentX() {
		return CENTER_ALIGNMENT;
	}

	public float getAlignmentY() {
		return CENTER_ALIGNMENT;
	}

	public int getBaseline(int width, int height) {
		return -1;
	}

	public BaselineResizeBehavior getBaselineResizeBehavior() {
		return BaselineResizeBehavior.OTHER;
	}


	public void doLayout() {
		layout();
	}

	public void layout() {
	}

	public void validate() {

	}

	public void invalidate() {

	}

	public void revalidate() {

	}

	public Graphics getGraphics() {
		return null;
	}

	public FontMetrics getFontMetrics(Font font) {
		return null;
	}

	Cursor cursor = null;

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public boolean isCursorSet() {
		return cursor != null;
	}

	public void paint(Graphics g) {
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paintAll(Graphics g) {
		paint(g);
	}

	public void repaint() {
		repaint(0, 0, 0, getWidth(), getHeight());
	}

	public void repaint(long tm) {
		repaint(tm, 0, 0, getWidth(), getHeight());
	}

	public void repaint(int x, int y, int width, int height) {
		repaint(0, getX(), getY(), getWidth(), getHeight());
	}

	public void repaint(long tm, int x, int y, int width, int height) {
		update(new Graphics2D() {
		});
	}

	public void print(Graphics g) {
		paint(g);
	}

	public void printAll(Graphics g) {

	}

	void lightweightPrint(Graphics g) {
		print(g);
	}


	public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
		return false;
	}

	public Image createImage(ImageProducer producer) {
		throw new RuntimeException("Not implemented");
	}

	public Image createImage(int width, int height) {
		throw new RuntimeException("Not implemented");
	}

	public VolatileImage createVolatileImage(int width, int height) {
		throw new RuntimeException("Not implemented");
	}

	public VolatileImage createVolatileImage(int width, int height, ImageCapabilities caps) throws AWTException {
		throw new RuntimeException("Not implemented");
	}

	public boolean prepareImage(Image image, ImageObserver observer) {
		return prepareImage(image, -1, -1, observer);
	}

	public boolean prepareImage(Image image, int width, int height, ImageObserver observer) {
		return false;
	}

	public int checkImage(Image image, ImageObserver observer) {
		return checkImage(image, -1, -1, observer);
	}

	public int checkImage(Image image, int width, int height, ImageObserver observer) {
		return 0;
	}

	protected class FlipBufferStrategy extends BufferStrategy {
		protected int numBuffers; // = 0
		protected BufferCapabilities caps; // = null
		protected Image drawBuffer; // = null
		protected VolatileImage drawVBuffer; // = null
		protected boolean validatedContents; // = false

		protected FlipBufferStrategy(int numBuffers, BufferCapabilities caps) throws AWTException {

		}

		protected void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException {

		}

		protected Image getBackBuffer() {
			return null;
		}

		protected void flip(BufferCapabilities.FlipContents flipAction) {

		}

		protected void destroyBuffers() {

		}

		public BufferCapabilities getCapabilities() {
			return null;
		}

		public Graphics getDrawGraphics() {
			return null;
		}

		protected void revalidate() {

		}

		public boolean contentsLost() {
			return false;
		}

		public boolean contentsRestored() {
			return validatedContents;
		}

		public void show() {
			flip(caps.getFlipContents());
		}

		public void dispose() {

		}
	}

	protected class BltBufferStrategy extends BufferStrategy {

		@Override
		public BufferCapabilities getCapabilities() {
			return null;
		}

		@Override
		public Graphics getDrawGraphics() {
			return null;
		}

		@Override
		public boolean contentsLost() {
			return false;
		}

		@Override
		public boolean contentsRestored() {
			return false;
		}

		@Override
		public void show() {

		}
	}

	public void setIgnoreRepaint(boolean ignoreRepaint) {
	}

	public boolean getIgnoreRepaint() {
		return false;
	}

	public boolean contains(int x, int y) {
		return inside(x, y);
	}

	@Deprecated
	public boolean inside(int x, int y) {
		return (x >= 0) && (x < getWidth()) && (y >= 0) && (y < getHeight());
	}

	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	public Component getComponentAt(int x, int y) {
		return locate(x, y);
	}

	@Deprecated
	public Component locate(int x, int y) {
		return contains(x, y) ? this : null;
	}

	public Component getComponentAt(Point p) {
		return getComponentAt(p.x, p.y);
	}

	@Deprecated
	public void deliverEvent(Event e) {
		postEvent(e);
	}

	public final void dispatchEvent(AWTEvent e) {
	}

	@Deprecated
	public boolean postEvent(Event e) {
		return false;
	}

	public synchronized void addComponentListener(ComponentListener l) {
	}

	public synchronized void removeComponentListener(ComponentListener l) {

	}

	public synchronized ComponentListener[] getComponentListeners() {
		return new ComponentListener[0];
	}

	public synchronized void addFocusListener(FocusListener l) {
	}

	public synchronized void removeFocusListener(FocusListener l) {

	}

	public synchronized FocusListener[] getFocusListeners() {
		return new FocusListener[0];
	}

	public void addHierarchyListener(HierarchyListener l) {

	}

	public void removeHierarchyListener(HierarchyListener l) {

	}

	public synchronized HierarchyListener[] getHierarchyListeners() {
		return new HierarchyListener[0];
	}

	public void addHierarchyBoundsListener(HierarchyBoundsListener l) {

	}

	public void removeHierarchyBoundsListener(HierarchyBoundsListener l) {

	}

	public synchronized HierarchyBoundsListener[] getHierarchyBoundsListeners() {
		return new HierarchyBoundsListener[0];
	}

	public synchronized void addKeyListener(KeyListener l) {

	}

	public synchronized void removeKeyListener(KeyListener l) {

	}

	public synchronized KeyListener[] getKeyListeners() {
		return new KeyListener[0];
	}

	private ArrayList<MouseListener> mouseListeners;

	public synchronized void addMouseListener(MouseListener l) {
		if (mouseListeners == null) mouseListeners = new ArrayList<>();
		widget.watchMouseEvents();
		mouseListeners.add(l);
	}

	public synchronized void removeMouseListener(MouseListener l) {

	}

	public synchronized MouseListener[] getMouseListeners() {
		return new MouseListener[0];
	}

	public synchronized void addMouseMotionListener(MouseMotionListener l) {
	}

	public synchronized void removeMouseMotionListener(MouseMotionListener l) {
	}

	public synchronized MouseMotionListener[] getMouseMotionListeners() {
		return new MouseMotionListener[0];
	}

	public synchronized void addMouseWheelListener(MouseWheelListener l) {

	}

	public synchronized void removeMouseWheelListener(MouseWheelListener l) {

	}

	public synchronized MouseWheelListener[] getMouseWheelListeners() {
		return new MouseWheelListener[0];
	}

	public synchronized void addInputMethodListener(InputMethodListener l) {

	}

	public synchronized void removeInputMethodListener(InputMethodListener l) {

	}

	public synchronized InputMethodListener[] getInputMethodListeners() {
		return new InputMethodListener[0];
	}

	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return null;
	}

	public InputMethodRequests getInputMethodRequests() {
		return null;
	}

	public InputContext getInputContext() {
		return null;
	}

	protected final void enableEvents(long eventsToEnable) {

	}

	protected final void disableEvents(long eventsToDisable) {

	}

	protected AWTEvent coalesceEvents(AWTEvent existingEvent,
									  AWTEvent newEvent) {
		return null;
	}

	protected void processEvent(AWTEvent e) {

	}

	protected void processComponentEvent(ComponentEvent e) {

	}

	protected void processFocusEvent(FocusEvent e) {

	}

	protected void processKeyEvent(KeyEvent e) {

	}

	protected void processMouseEvent(MouseEvent e) {
		if (mouseListeners != null) {
			for (MouseListener listener : mouseListeners) {
				switch (e.getID()) {
					case MouseEvent.MOUSE_CLICKED:
						listener.mouseClicked(e);
						break;
				}
			}
		}
	}

	protected void processMouseMotionEvent(MouseEvent e) {

	}

	protected void processMouseWheelEvent(MouseWheelEvent e) {

	}

	protected void processInputMethodEvent(InputMethodEvent e) {
	}

	protected void processHierarchyEvent(HierarchyEvent e) {

	}

	protected void processHierarchyBoundsEvent(HierarchyEvent e) {

	}

	@Deprecated
	public boolean handleEvent(Event evt) {
		return false;
	}

	@Deprecated
	public boolean mouseDown(Event evt, int x, int y) {
		return false;
	}

	@Deprecated
	public boolean mouseDrag(Event evt, int x, int y) {
		return false;
	}

	@Deprecated
	public boolean mouseUp(Event evt, int x, int y) {
		return false;
	}

	@Deprecated
	public boolean mouseMove(Event evt, int x, int y) {
		return false;
	}

	@Deprecated
	public boolean mouseEnter(Event evt, int x, int y) {
		return false;
	}

	@Deprecated
	public boolean mouseExit(Event evt, int x, int y) {
		return false;
	}

	@Deprecated
	public boolean keyDown(Event evt, int key) {
		return false;
	}

	@Deprecated
	public boolean keyUp(Event evt, int key) {
		return false;
	}

	@Deprecated
	public boolean action(Event evt, Object what) {
		return false;
	}

	public void addNotify() {

	}

	public void removeNotify() {

	}

	@Deprecated
	public boolean gotFocus(Event evt, Object what) {
		return false;
	}

	@Deprecated
	public boolean lostFocus(Event evt, Object what) {
		return false;
	}

	@Deprecated
	public boolean isFocusTraversable() {
		return true;
	}

	public boolean isFocusable() {
		return isFocusTraversable();
	}

	public void setFocusable(boolean focusable) {
	}

	public void setFocusTraversalKeys(int id, Set<? extends AWTKeyStroke> keystrokes) {
	}

	public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
		return new HashSet<>();
	}

	public boolean areFocusTraversalKeysSet(int id) {
		return false;
	}

	public void setFocusTraversalKeysEnabled(boolean focusTraversalKeysEnabled) {
	}

	public boolean getFocusTraversalKeysEnabled() {
		return true;
	}

	public void requestFocus() {

	}

	protected boolean requestFocus(boolean temporary) {
		return true;
	}

	public boolean requestFocusInWindow() {
		return true;
	}

	protected boolean requestFocusInWindow(boolean temporary) {
		return true;
	}

	public Container getFocusCycleRootAncestor() {
		return null;
	}

	public boolean isFocusCycleRoot(Container container) {
		return false;
	}

	public void transferFocus() {
		nextFocus();
	}

	@Deprecated
	public void nextFocus() {
		transferFocus();
	}

	public void transferFocusBackward() {

	}

	public void transferFocusUpCycle() {

	}

	public boolean hasFocus() {
		return false;
	}

	public boolean isFocusOwner() {
		return hasFocus();
	}

	public void add(PopupMenu popup) {

	}

	public void remove(MenuComponent popup) {

	}

	protected String paramString() {
		return "";
	}

	public String toString() {
		return getClass().getName() + '[' + paramString() + ']';
	}

	public void list() {
		list(System.out, 0);
	}

	public void list(PrintStream out) {
		list(out, 0);
	}

	public void list(PrintStream out, int indent) {

	}

	public void list(PrintWriter out) {
		list(out, 0);
	}

	public void list(PrintWriter out, int indent) {

	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {

	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return new PropertyChangeListener[0];
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {

	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {

	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return new PropertyChangeListener[0];
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {

	}

	protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	}

	protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
	}

	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {
	}

	public void firePropertyChange(String propertyName, char oldValue, char newValue) {

	}

	public void firePropertyChange(String propertyName, short oldValue, short newValue) {

	}

	public void firePropertyChange(String propertyName, long oldValue, long newValue) {

	}

	public void firePropertyChange(String propertyName, float oldValue, float newValue) {

	}

	public void firePropertyChange(String propertyName, double oldValue, double newValue) {

	}

	public void setComponentOrientation(ComponentOrientation o) {

	}

	public ComponentOrientation getComponentOrientation() {
		return ComponentOrientation.LEFT_TO_RIGHT;
	}

	public void applyComponentOrientation(ComponentOrientation orientation) {

	}

	public AccessibleContext getAccessibleContext() {
		return null;
	}

	protected abstract class AccessibleAWTComponent extends AccessibleContext implements Serializable, AccessibleComponent {
	}
}