package java.awt;

import java.util.HashMap;

public class RenderingHints {
	public abstract static class Key {
	}

	HashMap<Object, Object> hintmap = new HashMap<>(7);

	public static final RenderingHints.Key KEY_ANTIALIASING = null;
	public static final Object VALUE_ANTIALIAS_ON = null;
	public static final Object VALUE_ANTIALIAS_OFF = null;
	public static final Object VALUE_ANTIALIAS_DEFAULT = null;

	public static final RenderingHints.Key KEY_RENDERING = null;
	public static final Object VALUE_RENDER_SPEED = null;
	public static final Object VALUE_RENDER_QUALITY = null;
	public static final Object VALUE_RENDER_DEFAULT = null;

	public static final RenderingHints.Key KEY_DITHERING = null;
	public static final Object VALUE_DITHER_DISABLE = null;
	public static final Object VALUE_DITHER_ENABLE = null;
	public static final Object VALUE_DITHER_DEFAULT = null;

	public static final RenderingHints.Key KEY_TEXT_ANTIALIASING = null;
	public static final Object VALUE_TEXT_ANTIALIAS_ON = null;
	public static final Object VALUE_TEXT_ANTIALIAS_OFF = null;
	public static final Object VALUE_TEXT_ANTIALIAS_DEFAULT = null;
	public static final Object VALUE_TEXT_ANTIALIAS_GASP = null;
	public static final Object VALUE_TEXT_ANTIALIAS_LCD_HRGB = null;
	public static final Object VALUE_TEXT_ANTIALIAS_LCD_HBGR = null;
	public static final Object VALUE_TEXT_ANTIALIAS_LCD_VRGB = null;
	public static final Object VALUE_TEXT_ANTIALIAS_LCD_VBGR = null;

	public static final RenderingHints.Key KEY_TEXT_LCD_CONTRAST = null;

	public static final RenderingHints.Key KEY_FRACTIONALMETRICS = null;
	public static final Object VALUE_FRACTIONALMETRICS_OFF = null;
	public static final Object VALUE_FRACTIONALMETRICS_ON = null;
	public static final Object VALUE_FRACTIONALMETRICS_DEFAULT = null;

	public static final RenderingHints.Key KEY_INTERPOLATION = null;
	public static final Object VALUE_INTERPOLATION_NEAREST_NEIGHBOR = null;
	public static final Object VALUE_INTERPOLATION_BILINEAR = null;
	public static final Object VALUE_INTERPOLATION_BICUBIC = null;

	public static final RenderingHints.Key KEY_ALPHA_INTERPOLATION = null;
	public static final Object VALUE_ALPHA_INTERPOLATION_SPEED = null;
	public static final Object VALUE_ALPHA_INTERPOLATION_QUALITY = null;
	public static final Object VALUE_ALPHA_INTERPOLATION_DEFAULT = null;

	public static final RenderingHints.Key KEY_COLOR_RENDERING = null;
	public static final Object VALUE_COLOR_RENDER_SPEED = null;
	public static final Object VALUE_COLOR_RENDER_QUALITY = null;
	public static final Object VALUE_COLOR_RENDER_DEFAULT = null;

	public static final RenderingHints.Key KEY_STROKE_CONTROL = null;
	public static final Object VALUE_STROKE_DEFAULT = null;
	public static final Object VALUE_STROKE_NORMALIZE = null;
	public static final Object VALUE_STROKE_PURE = null;
}
