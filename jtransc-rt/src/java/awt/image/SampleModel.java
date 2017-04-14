package java.awt.image;

@SuppressWarnings("WeakerAccess")
public abstract class SampleModel {
	protected int width;
	protected int height;
	protected int numBands;
	protected int dataType;

	public SampleModel(int dataType, int w, int h, int numBands) {
		long size = (long) w * h;
		this.dataType = dataType;
		this.width = w;
		this.height = h;
		this.numBands = numBands;
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

	final public int getDataType() {
		return dataType;
	}

	public int getTransferType() {
		return dataType;
	}

	native public int[] getPixel(int x, int y, int iArray[], DataBuffer data);

	native public Object getDataElements(int x, int y, int w, int h, Object obj, DataBuffer data);

	native public void setDataElements(int x, int y, int w, int h, Object obj, DataBuffer data);

	native public float[] getPixel(int x, int y, float fArray[], DataBuffer data);

	native public double[] getPixel(int x, int y, double dArray[], DataBuffer data);

	native public int[] getPixels(int x, int y, int w, int h, int iArray[], DataBuffer data);

	native public float[] getPixels(int x, int y, int w, int h, float fArray[], DataBuffer data);

	native public double[] getPixels(int x, int y, int w, int h, double dArray[], DataBuffer data);

	native public float getSampleFloat(int x, int y, int b, DataBuffer data);

	native public double getSampleDouble(int x, int y, int b, DataBuffer data);

	native public int[] getSamples(int x, int y, int w, int h, int b, int iArray[], DataBuffer data);

	native public float[] getSamples(int x, int y, int w, int h, int b, float fArray[], DataBuffer data);

	native public double[] getSamples(int x, int y, int w, int h, int b, double dArray[], DataBuffer data);

	native public void setPixel(int x, int y, int iArray[], DataBuffer data);

	native public void setPixel(int x, int y, float fArray[], DataBuffer data);

	native public void setPixel(int x, int y, double dArray[], DataBuffer data);

	native public void setPixels(int x, int y, int w, int h, int iArray[], DataBuffer data);

	native public void setPixels(int x, int y, int w, int h, float fArray[], DataBuffer data);

	native public void setPixels(int x, int y, int w, int h, double dArray[], DataBuffer data);

	native public void setSample(int x, int y, int b, float s, DataBuffer data);

	native public void setSample(int x, int y, int b, double s, DataBuffer data);

	native public void setSamples(int x, int y, int w, int h, int b, int iArray[], DataBuffer data);

	native public void setSamples(int x, int y, int w, int h, int b, float fArray[], DataBuffer data);

	native public void setSamples(int x, int y, int w, int h, int b, double dArray[], DataBuffer data);

	public abstract int getNumDataElements();

	public abstract Object getDataElements(int x, int y, Object obj, DataBuffer data);

	public abstract void setDataElements(int x, int y, Object obj, DataBuffer data);

	public abstract int getSample(int x, int y, int b, DataBuffer data);

	public abstract void setSample(int x, int y, int b, int s, DataBuffer data);

	public abstract SampleModel createCompatibleSampleModel(int w, int h);

	public abstract SampleModel createSubsetSampleModel(int bands[]);

	public abstract DataBuffer createDataBuffer();

	public abstract int[] getSampleSize();

	public abstract int getSampleSize(int band);
}
