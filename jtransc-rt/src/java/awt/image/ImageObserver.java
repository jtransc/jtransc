package java.awt.image;

import java.awt.*;

public interface ImageObserver {
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height);

	public static final int WIDTH = 1;
	public static final int HEIGHT = 2;
	public static final int PROPERTIES = 4;
	public static final int SOMEBITS = 8;
	public static final int FRAMEBITS = 16;
	public static final int ALLBITS = 32;
	public static final int ERROR = 64;
	public static final int ABORT = 128;
}
