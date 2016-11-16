package java.awt.image;

import java.awt.color.ColorSpace;

@SuppressWarnings("WeakerAccess")
public abstract class ColorModel {
	public ColorModel(int bits) {
	}

	protected ColorModel(int pixel_bits, int[] bits, ColorSpace cspace, boolean hasAlpha, boolean isAlphaPremultiplied, int transparency, int transferType) {
	}

	public abstract int getRed(int pixel);

	public abstract int getGreen(int pixel);

	public abstract int getBlue(int pixel);

	public abstract int getAlpha(int pixel);
}
