package java.awt;

import java.util.Locale;
import java.util.ResourceBundle;

public final class ComponentOrientation implements java.io.Serializable {
	private static final int UNK_BIT = 1;
	private static final int HORIZ_BIT = 2;
	private static final int LTR_BIT = 4;

	public static final ComponentOrientation LEFT_TO_RIGHT = new ComponentOrientation(HORIZ_BIT | LTR_BIT);
	public static final ComponentOrientation RIGHT_TO_LEFT = new ComponentOrientation(HORIZ_BIT);
	public static final ComponentOrientation UNKNOWN = new ComponentOrientation(HORIZ_BIT | LTR_BIT | UNK_BIT);

	public boolean isHorizontal() {
		return (orientation & HORIZ_BIT) != 0;
	}

	public boolean isLeftToRight() {
		return (orientation & LTR_BIT) != 0;
	}

	public static ComponentOrientation getOrientation(Locale locale) {
		return ComponentOrientation.LEFT_TO_RIGHT;
	}

	public static ComponentOrientation getOrientation(ResourceBundle bdl) {
		return ComponentOrientation.LEFT_TO_RIGHT;
	}

	private int orientation;

	private ComponentOrientation(int value) {
		orientation = value;
	}
}
