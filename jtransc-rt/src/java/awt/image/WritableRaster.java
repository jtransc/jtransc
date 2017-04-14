package java.awt.image;

import java.awt.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public class WritableRaster extends Raster {
	protected WritableRaster(SampleModel sampleModel, Point origin) {
		this(sampleModel, sampleModel.createDataBuffer(), new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
	}

	protected WritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Point origin) {
		this(sampleModel, dataBuffer, new Rectangle(origin.x, origin.y, sampleModel.getWidth(), sampleModel.getHeight()), origin, null);
	}

	protected WritableRaster(SampleModel sampleModel, DataBuffer dataBuffer, Rectangle aRegion, Point sampleModelTranslate, WritableRaster parent) {
		super(sampleModel, dataBuffer, aRegion, sampleModelTranslate, parent);
	}

	native public WritableRaster getWritableParent();

	native public WritableRaster createWritableTranslatedChild(int childMinX, int childMinY);

	native public WritableRaster createWritableChild(int parentX, int parentY, int w, int h, int childMinX, int childMinY, int bandList[]);

	native public void setDataElements(int x, int y, Object inData);

	native public void setDataElements(int x, int y, Raster inRaster);

	native public void setDataElements(int x, int y, int w, int h, Object inData);

	native public void setRect(Raster srcRaster);

	native public void setRect(int dx, int dy, Raster srcRaster);

	native public void setPixel(int x, int y, int iArray[]);

	native public void setPixel(int x, int y, float fArray[]);

	native public void setPixel(int x, int y, double dArray[]);

	native public void setPixels(int x, int y, int w, int h, int iArray[]);

	native public void setPixels(int x, int y, int w, int h, float fArray[]);

	native public void setPixels(int x, int y, int w, int h, double dArray[]);

	native public void setSample(int x, int y, int b, int s);

	native public void setSample(int x, int y, int b, float s);

	native public void setSample(int x, int y, int b, double s);

	native public void setSamples(int x, int y, int w, int h, int b, int iArray[]);

	native public void setSamples(int x, int y, int w, int h, int b, float fArray[]);

	native public void setSamples(int x, int y, int w, int h, int b, double dArray[]);

}