package java.awt.image;

import java.awt.*;

@SuppressWarnings("WeakerAccess")
public class Raster {
	protected SampleModel sampleModel;
	protected DataBuffer dataBuffer;
	protected int minX;
	protected int minY;
	protected int width;
	protected int height;
	protected int sampleModelTranslateX;
	protected int sampleModelTranslateY;
	protected int numBands;
	protected int numDataElements;
	protected Raster parent;

	protected Raster(SampleModel sampleModel, Point origin) {
	}

	protected Raster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
	}

	protected Raster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion, Point sampleModelTranslate, Raster parent) {
	}

	native public static WritableRaster createInterleavedRaster(int dataType, int w, int h, int bands, Point location);

	native public static WritableRaster createInterleavedRaster(int dataType, int w, int h, int scanlineStride, int pixelStride, int bandOffsets[], Point location);

	native public static WritableRaster createBandedRaster(int dataType, int w, int h, int bands, Point location);

	native public static WritableRaster createBandedRaster(int dataType, int w, int h, int scanlineStride, int bankIndices[], int bandOffsets[], Point location);

	native public static WritableRaster createPackedRaster(int dataType, int w, int h, int bandMasks[], Point location);

	native public static WritableRaster createPackedRaster(int dataType, int w, int h, int bands, int bitsPerBand, Point location);

	native public static WritableRaster createInterleavedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride, int pixelStride, int bandOffsets[], Point location);

	native public static WritableRaster createBandedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride, int bankIndices[], int bandOffsets[], Point location);

	native public static WritableRaster createPackedRaster(DataBuffer dataBuffer, int w, int h, int scanlineStride, int bandMasks[], Point location);

	native public static WritableRaster createPackedRaster(DataBuffer dataBuffer, int w, int h, int bitsPerPixel, Point location);

	native public static Raster createRaster(SampleModel sm, DataBuffer db, Point location);

	native public static WritableRaster createWritableRaster(SampleModel sm, Point location);

	native public static WritableRaster createWritableRaster(SampleModel sm, DataBuffer db, Point location);

	public Raster getParent() {
		return parent;
	}

	final public int getSampleModelTranslateX() {
		return sampleModelTranslateX;
	}

	final public int getSampleModelTranslateY() {
		return sampleModelTranslateY;
	}

	native public WritableRaster createCompatibleWritableRaster();

	native public WritableRaster createCompatibleWritableRaster(int w, int h);

	native public WritableRaster createCompatibleWritableRaster(Rectangle rect);

	native public WritableRaster createCompatibleWritableRaster(int x, int y, int w, int h);

	native public Raster createTranslatedChild(int childMinX, int childMinY);

	native public Raster createChild(int parentX, int parentY, int width, int height, int childMinX, int childMinY, int bandList[]);

	public Rectangle getBounds() {
		return new Rectangle(minX, minY, width, height);
	}

	final public int getMinX() {
		return minX;
	}

	final public int getMinY() {
		return minY;
	}

	final public int getWidth() {
		return width;
	}

	final public int getHeight() {
		return height;
	}

	final public int getNumBands() {
		return numBands;
	}

	final public int getNumDataElements() {
		return sampleModel.getNumDataElements();
	}

	final public int getTransferType() {
		return sampleModel.getTransferType();
	}

	public DataBuffer getDataBuffer() {
		return dataBuffer;
	}

	public SampleModel getSampleModel() {
		return sampleModel;
	}

	native public Object getDataElements(int x, int y, Object outData);

	native public Object getDataElements(int x, int y, int w, int h, Object outData);

	native public int[] getPixel(int x, int y, int iArray[]);

	native public float[] getPixel(int x, int y, float fArray[]);

	native public double[] getPixel(int x, int y, double dArray[]);

	native public int[] getPixels(int x, int y, int w, int h, int iArray[]);

	native public float[] getPixels(int x, int y, int w, int h, float fArray[]);

	native public double[] getPixels(int x, int y, int w, int h, double dArray[]);

	native public int getSample(int x, int y, int b);

	native public float getSampleFloat(int x, int y, int b);

	native public double getSampleDouble(int x, int y, int b);

	native public int[] getSamples(int x, int y, int w, int h, int b, int iArray[]);

	native public float[] getSamples(int x, int y, int w, int h, int b, float fArray[]);

	native public double[] getSamples(int x, int y, int w, int h, int b, double dArray[]);
}
