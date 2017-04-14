package java.awt;

import java.awt.image.ImageObserver;

public abstract class Graphics {
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		return false;
	}
}
