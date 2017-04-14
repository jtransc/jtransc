package java.awt.event;

import java.awt.*;
import java.lang.annotation.Native;

public class MouseWheelEvent extends MouseEvent {
	public static final int WHEEL_UNIT_SCROLL = 0;
	public static final int WHEEL_BLOCK_SCROLL = 1;

	int scrollType;
	int scrollAmount;
	int wheelRotation;
	double preciseWheelRotation;

	public MouseWheelEvent (Component source, int id, long when, int modifiers,
							int x, int y, int clickCount, boolean popupTrigger,
							int scrollType, int scrollAmount, int wheelRotation) {

		this(source, id, when, modifiers, x, y, 0, 0, clickCount,
			popupTrigger, scrollType, scrollAmount, wheelRotation);
	}

	public MouseWheelEvent (Component source, int id, long when, int modifiers,
							int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger,
							int scrollType, int scrollAmount, int wheelRotation) {

		this(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger,
			scrollType, scrollAmount, wheelRotation, wheelRotation);

	}

	public MouseWheelEvent (Component source, int id, long when, int modifiers,
							int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger,
							int scrollType, int scrollAmount, int wheelRotation, double preciseWheelRotation) {

		super(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount,
			popupTrigger, MouseEvent.NOBUTTON);

		this.scrollType = scrollType;
		this.scrollAmount = scrollAmount;
		this.wheelRotation = wheelRotation;
		this.preciseWheelRotation = preciseWheelRotation;
	}

}
