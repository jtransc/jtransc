package java.awt.image;

import java.awt.color.ColorSpace;

@SuppressWarnings("WeakerAccess")
public abstract class PackedColorModel extends ColorModel {
	public PackedColorModel(ColorSpace space, int bits, int[] colorMaskArray, int alphaMask, boolean isAlphaPremultiplied, int trans, int transferType) {
		super(bits, new int[0], space, alphaMask != 0, isAlphaPremultiplied, trans, transferType);
	}

	public PackedColorModel(ColorSpace space, int bits, int rmask, int gmask, int bmask, int amask, boolean isAlphaPremultiplied, int trans, int transferType) {
		super(bits, new int[0], space, amask != 0, isAlphaPremultiplied, trans, transferType);
	}
}
