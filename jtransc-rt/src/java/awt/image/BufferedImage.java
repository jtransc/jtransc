package java.awt.image;

import java.awt.*;

@SuppressWarnings({"WeakerAccess", "unused", "FieldCanBeLocal"})
public class BufferedImage extends Image {
	public static final int TYPE_CUSTOM = 0;
	public static final int TYPE_INT_RGB = 1;
	public static final int TYPE_INT_ARGB = 2;
	public static final int TYPE_INT_ARGB_PRE = 3;
	public static final int TYPE_INT_BGR = 4;
	public static final int TYPE_3BYTE_BGR = 5;
	public static final int TYPE_4BYTE_ABGR = 6;
	public static final int TYPE_4BYTE_ABGR_PRE = 7;
	public static final int TYPE_USHORT_565_RGB = 8;
	public static final int TYPE_USHORT_555_RGB = 9;
	public static final int TYPE_BYTE_GRAY = 10;
	public static final int TYPE_USHORT_GRAY = 11;
	public static final int TYPE_BYTE_BINARY = 12;

	public static final int TYPE_BYTE_INDEXED = 13;

	private int imageType = TYPE_CUSTOM;
	private int width;
	private int height;
	private int[] data;
	private Graphics2D graphics;

	public BufferedImage(int width, int height, int imageType) {
		this.width = width;
		this.height = height;
		this.imageType = imageType;
		this.data = new int[width * height];
	}

	private int index(int x, int y) {
		return y * width + x;
	}

	public int getRGB(int x, int y) {
		return data[index(x, y)];
	}

	public int[] getRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
		if (rgbArray == null) rgbArray = new int[offset + h * scansize];
		for (int y = 0; y < h; y++) {
			int n = offset + y * scansize;
			for (int x = 0; x < w; x++) {
				rgbArray[n++] = getRGB(startX + x, startY + y);
			}
		}
		return rgbArray;
	}

	public synchronized void setRGB(int x, int y, int rgb) {
		data[index(x, y)] = rgb;
	}

	public void setRGB(int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
		for (int y = 0; y < h; y++) {
			int n = offset + y * scansize;
			for (int x = 0; x < w; x++) {
				setRGB(startX + x, startY + y, rgbArray[n++]);
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth(ImageObserver observer) {
		return width;
	}

	@Override
	public int getHeight(ImageObserver observer) {
		return height;
	}

	@Override
	public ImageProducer getSource() {
		return null;
	}

	@Override
	public Graphics getGraphics() {
		if (graphics == null) {
			graphics = new Graphics2D() {
				public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
					if (img instanceof BufferedImage) {
						BufferedImage bi = (BufferedImage) img;
						int[] rgb = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth());
						setRGB(x, y, bi.getWidth(), bi.getHeight(), rgb, 0, bi.getWidth());
					}
					return false;
				}
			};
		}
		return graphics;
	}

	@Override
	public Object getProperty(String name, ImageObserver observer) {
		return null;
	}
}
