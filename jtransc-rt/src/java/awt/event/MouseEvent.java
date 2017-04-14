package java.awt.event;

import java.awt.*;

@SuppressWarnings("PointlessArithmeticExpression")
public class MouseEvent extends InputEvent {
	public static final int MOUSE_FIRST = 500;
	public static final int MOUSE_LAST = 507;
	public static final int MOUSE_CLICKED = 0 + MOUSE_FIRST;
	public static final int MOUSE_PRESSED = 1 + MOUSE_FIRST; //Event.MOUSE_DOWN
	public static final int MOUSE_RELEASED = 2 + MOUSE_FIRST; //Event.MOUSE_UP
	public static final int MOUSE_MOVED = 3 + MOUSE_FIRST; //Event.MOUSE_MOVE
	public static final int MOUSE_ENTERED = 4 + MOUSE_FIRST; //Event.MOUSE_ENTER
	public static final int MOUSE_EXITED = 5 + MOUSE_FIRST; //Event.MOUSE_EXIT
	public static final int MOUSE_DRAGGED = 6 + MOUSE_FIRST; //Event.MOUSE_DRAG
	public static final int MOUSE_WHEEL = 7 + MOUSE_FIRST;
	public static final int NOBUTTON = 0;
	public static final int BUTTON1 = 1;
	public static final int BUTTON2 = 2;
	public static final int BUTTON3 = 3;
	int x;
	int y;
	private int xAbs;
	private int yAbs;
	int clickCount;
	int button;
	boolean popupTrigger = false;

	public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger) {
		this(source, id, when, modifiers, x, y, clickCount, popupTrigger, NOBUTTON);
	}

	public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger, int button) {
		this(source, id, when, modifiers, x, y, 0, 0, clickCount, popupTrigger, button);
		//Point eventLocationOnScreen = new Point(0, 0);
		//try {
		//	eventLocationOnScreen = source.getLocationOnScreen();
		//	this.xAbs = eventLocationOnScreen.x + x;
		//	this.yAbs = eventLocationOnScreen.y + y;
		//} catch (IllegalComponentStateException e) {
		//	this.xAbs = 0;
		//	this.yAbs = 0;
		//}
	}

	public MouseEvent(Component source, int id, long when, int modifiers, int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger, int button) {
		super(source, id, when, modifiers);
		this.x = x;
		this.y = y;
		this.xAbs = xAbs;
		this.yAbs = yAbs;
		this.clickCount = clickCount;
		this.popupTrigger = popupTrigger;
		this.button = button;
	}
}
