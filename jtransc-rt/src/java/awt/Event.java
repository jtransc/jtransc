package java.awt;

public class Event {
	public static final int SHIFT_MASK = 1 << 0;
	public static final int CTRL_MASK = 1 << 1;
	public static final int META_MASK = 1 << 2;
	public static final int ALT_MASK = 1 << 3;


	public static final int HOME = 1000;
	public static final int END = 1001;
	public static final int PGUP = 1002;
	public static final int PGDN = 1003;
	public static final int UP = 1004;
	public static final int DOWN = 1005;
	public static final int LEFT = 1006;
	public static final int RIGHT = 1007;
	public static final int F1 = 1008;
	public static final int F2 = 1009;
	public static final int F3 = 1010;
	public static final int F4 = 1011;
	public static final int F5 = 1012;
	public static final int F6 = 1013;
	public static final int F7 = 1014;
	public static final int F8 = 1015;
	public static final int F9 = 1016;
	public static final int F10 = 1017;
	public static final int F11 = 1018;
	public static final int F12 = 1019;
	public static final int PRINT_SCREEN = 1020;
	public static final int SCROLL_LOCK = 1021;
	public static final int CAPS_LOCK = 1022;
	public static final int NUM_LOCK = 1023;
	public static final int PAUSE = 1024;
	public static final int INSERT = 1025;


	public static final int ENTER = '\n';
	public static final int BACK_SPACE = '\b';
	public static final int TAB = '\t';
	public static final int ESCAPE = 27;
	public static final int DELETE = 127;


	private static final int WINDOW_EVENT = 200;
	public static final int WINDOW_DESTROY = 1 + WINDOW_EVENT;
	public static final int WINDOW_EXPOSE = 2 + WINDOW_EVENT;
	public static final int WINDOW_ICONIFY = 3 + WINDOW_EVENT;
	public static final int WINDOW_DEICONIFY = 4 + WINDOW_EVENT;
	public static final int WINDOW_MOVED = 5 + WINDOW_EVENT;


	private static final int KEY_EVENT = 400;
	public static final int KEY_PRESS = 1 + KEY_EVENT;
	public static final int KEY_RELEASE = 2 + KEY_EVENT;
	public static final int KEY_ACTION = 3 + KEY_EVENT;
	public static final int KEY_ACTION_RELEASE = 4 + KEY_EVENT;

	private static final int MOUSE_EVENT = 500;
	public static final int MOUSE_DOWN = 1 + MOUSE_EVENT;
	public static final int MOUSE_UP = 2 + MOUSE_EVENT;
	public static final int MOUSE_MOVE = 3 + MOUSE_EVENT;
	public static final int MOUSE_ENTER = 4 + MOUSE_EVENT;
	public static final int MOUSE_EXIT = 5 + MOUSE_EVENT;
	public static final int MOUSE_DRAG = 6 + MOUSE_EVENT;


	private static final int SCROLL_EVENT = 600;
	public static final int SCROLL_LINE_UP = 1 + SCROLL_EVENT;
	public static final int SCROLL_LINE_DOWN = 2 + SCROLL_EVENT;
	public static final int SCROLL_PAGE_UP = 3 + SCROLL_EVENT;
	public static final int SCROLL_PAGE_DOWN = 4 + SCROLL_EVENT;
	public static final int SCROLL_ABSOLUTE = 5 + SCROLL_EVENT;
	public static final int SCROLL_BEGIN = 6 + SCROLL_EVENT;
	public static final int SCROLL_END = 7 + SCROLL_EVENT;

	private static final int LIST_EVENT = 700;
	public static final int LIST_SELECT = 1 + LIST_EVENT;
	public static final int LIST_DESELECT = 2 + LIST_EVENT;
	private static final int MISC_EVENT = 1000;
	public static final int ACTION_EVENT = 1 + MISC_EVENT;
	public static final int LOAD_FILE = 2 + MISC_EVENT;
	public static final int SAVE_FILE = 3 + MISC_EVENT;
	public static final int GOT_FOCUS = 4 + MISC_EVENT;
	public static final int LOST_FOCUS = 5 + MISC_EVENT;
}