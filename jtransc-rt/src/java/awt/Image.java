package java.awt;

import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

abstract public class Image {
	abstract public int getWidth(ImageObserver observer);
	abstract public int getHeight(ImageObserver observer);
	abstract public ImageProducer getSource();
	abstract public Graphics getGraphics();
	abstract public Object getProperty(String name, ImageObserver observer);
}
