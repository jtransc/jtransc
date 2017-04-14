package java.awt.image;

import java.awt.*;
import java.util.Vector;

public interface RenderedImage {
	Vector<RenderedImage> getSources();
	Object getProperty(String name);
	String[] getPropertyNames();
	ColorModel getColorModel();
	SampleModel getSampleModel();
	int getWidth();
	int getHeight();
	int getMinX();
	int getMinY();
	int getNumXTiles();
	int getNumYTiles();
	int getMinTileX();
	int getMinTileY();
	int getTileWidth();
	int getTileHeight();
	int getTileGridXOffset();
	int getTileGridYOffset();
	Raster getTile(int tileX, int tileY);
	Raster getData();
	Raster getData(Rectangle rect);
	WritableRaster copyData(WritableRaster raster);
}
