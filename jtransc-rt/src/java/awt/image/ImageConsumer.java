package java.awt.image;

import java.util.Hashtable;

public interface ImageConsumer {
	void setDimensions(int width, int height);

	void setProperties(Hashtable<?, ?> props);

	void setColorModel(ColorModel model);

	void setHints(int hintflags);

	int RANDOMPIXELORDER = 1;
	int TOPDOWNLEFTRIGHT = 2;
	int COMPLETESCANLINES = 4;
	int SINGLEPASS = 8;
	int SINGLEFRAME = 16;

	void setPixels(int x, int y, int w, int h, ColorModel model, byte pixels[], int off, int scansize);

	void setPixels(int x, int y, int w, int h, ColorModel model, int pixels[], int off, int scansize);

	void imageComplete(int status);

	int IMAGEERROR = 1;
	int SINGLEFRAMEDONE = 2;
	int STATICIMAGEDONE = 3;
	int IMAGEABORTED = 4;
}
