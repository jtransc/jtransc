package java.awt.color;

public abstract class ColorSpace {
	public static final int TYPE_XYZ = 0;
	public static final int TYPE_Lab = 1;
	public static final int TYPE_Luv = 2;
	public static final int TYPE_YCbCr = 3;
	public static final int TYPE_Yxy = 4;
	public static final int TYPE_RGB = 5;
	public static final int TYPE_GRAY = 6;
	public static final int TYPE_HSV = 7;
	public static final int TYPE_HLS = 8;
	public static final int TYPE_CMYK = 9;
	public static final int TYPE_CMY = 11;
	public static final int TYPE_2CLR = 12;
	public static final int TYPE_3CLR = 13;
	public static final int TYPE_4CLR = 14;
	public static final int TYPE_5CLR = 15;
	public static final int TYPE_6CLR = 16;
	public static final int TYPE_7CLR = 17;
	public static final int TYPE_8CLR = 18;
	public static final int TYPE_9CLR = 19;
	public static final int TYPE_ACLR = 20;
	public static final int TYPE_BCLR = 21;
	public static final int TYPE_CCLR = 22;
	public static final int TYPE_DCLR = 23;
	public static final int TYPE_ECLR = 24;
	public static final int TYPE_FCLR = 25;
	public static final int CS_sRGB = 1000;
	public static final int CS_LINEAR_RGB = 1004;
	public static final int CS_CIEXYZ = 1001;
	public static final int CS_PYCC = 1002;
	public static final int CS_GRAY = 1003;

	private int type;
	private int numcomponents;

	protected ColorSpace(int type, int numcomponents) {
		this.type = type;
		this.numcomponents = numcomponents;
	}

	native public static ColorSpace getInstance(int colorspace);

	public boolean isCS_sRGB() {
		return (this.type == CS_sRGB);
	}

	public abstract float[] toRGB(float[] colorvalue);


	public abstract float[] fromRGB(float[] rgbvalue);


	public abstract float[] toCIEXYZ(float[] colorvalue);


	public abstract float[] fromCIEXYZ(float[] colorvalue);

	public int getType() {
		return type;
	}

	public int getNumComponents() {
		return numcomponents;
	}

	public String getName(int idx) {
		return "ColorSpace" + idx;
	}

	public float getMinValue(int component) {
		return 0.0f;
	}

	public float getMaxValue(int component) {
		return 1.0f;
	}
}