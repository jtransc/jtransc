package javax.swing;

import java.awt.*;

public class ImageIcon implements Icon {
	Image image;

	public ImageIcon() {
	}

	public ImageIcon(Image image) {
		this.image = image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public Image getImage() {
		return image;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.drawImage(image, x, y, c);
	}

	@Override
	public int getIconWidth() {
		return (image != null) ? image.getWidth(null) : 0;
	}

	@Override
	public int getIconHeight() {
		return (image != null) ? image.getHeight(null) : 0;
	}
}
