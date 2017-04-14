package java.awt;

import java.io.Serializable;

@SuppressWarnings({"WeakerAccess", "PointlessBitwiseExpression"})
public class Color implements Paint, Serializable {
	public final static Color white = new Color(255, 255, 255);
	public final static Color WHITE = white;
	public final static Color lightGray = new Color(192, 192, 192);
	public final static Color LIGHT_GRAY = lightGray;
	public final static Color gray = new Color(128, 128, 128);
	public final static Color GRAY = gray;
	public final static Color darkGray = new Color(64, 64, 64);
	public final static Color DARK_GRAY = darkGray;
	public final static Color black = new Color(0, 0, 0);
	public final static Color BLACK = black;
	public final static Color red = new Color(255, 0, 0);
	public final static Color RED = red;
	public final static Color pink = new Color(255, 175, 175);
	public final static Color PINK = pink;
	public final static Color orange = new Color(255, 200, 0);
	public final static Color ORANGE = orange;
	public final static Color yellow = new Color(255, 255, 0);
	public final static Color YELLOW = yellow;
	public final static Color green = new Color(0, 255, 0);
	public final static Color GREEN = green;
	public final static Color magenta = new Color(255, 0, 255);
	public final static Color MAGENTA = magenta;
	public final static Color cyan = new Color(0, 255, 255);
	public final static Color CYAN = cyan;
	public final static Color blue = new Color(0, 0, 255);
	public final static Color BLUE = blue;

	private final int value;

	public Color(int r, int g, int b) {
		this(r, g, b, 255);
	}

	public Color(int r, int g, int b, int a) {
		this(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0));
	}

	public Color(int rgb) {
		value = 0xff000000 | rgb;
	}

	public Color(int rgba, boolean hasalpha) {
		if (!hasalpha) rgba |= 0xFF000000;
		value = rgba;
	}

	public Color(float r, float g, float b) {
		this((int) (r * 255 + 0.5), (int) (g * 255 + 0.5), (int) (b * 255 + 0.5));
	}

	public Color(float r, float g, float b, float a) {
		this((int) (r * 255 + 0.5), (int) (g * 255 + 0.5), (int) (b * 255 + 0.5), (int) (a * 255 + 0.5));
	}

	public int getRed() {
		return (value >> 16) & 0xFF;
	}

	public int getGreen() {
		return (value >> 8) & 0xFF;
	}

	public int getBlue() {
		return (value >> 0) & 0xFF;
	}

	public int getAlpha() {
		return (value >> 24) & 0xff;
	}

	public int getRGB() {
		return value;
	}

	//@Override
	//public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
	//	return null;
	//}

	@Override
	public int getTransparency() {
		return 0;
	}
}
