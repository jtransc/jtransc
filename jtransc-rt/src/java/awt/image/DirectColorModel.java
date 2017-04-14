package java.awt.image;

import java.awt.*;
import java.awt.color.ColorSpace;

@SuppressWarnings("WeakerAccess")
public class DirectColorModel extends PackedColorModel {
	public DirectColorModel(int bits, int rmask, int gmask, int bmask) {
		this(bits, rmask, gmask, bmask, 0);
	}

	public DirectColorModel(int bits, int rmask, int gmask, int bmask, int amask) {
		super(ColorSpace.getInstance(ColorSpace.CS_sRGB), bits, rmask, gmask, bmask, amask, false, amask == 0 ? Transparency.OPAQUE : Transparency.TRANSLUCENT, DataBuffer.TYPE_INT);
	}

	public DirectColorModel(ColorSpace space, int bits, int rmask, int gmask, int bmask, int amask, boolean isAlphaPremultiplied, int transferType) {
		super(space, bits, rmask, gmask, bmask, amask,
			isAlphaPremultiplied,
			amask == 0 ? Transparency.OPAQUE : Transparency.TRANSLUCENT,
			transferType);
	}


	@Override
	public int getRed(int pixel) {
		return 0;
	}

	@Override
	public int getGreen(int pixel) {
		return 0;
	}

	@Override
	public int getBlue(int pixel) {
		return 0;
	}

	@Override
	public int getAlpha(int pixel) {
		return 0;
	}
}
