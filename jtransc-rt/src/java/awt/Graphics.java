package java.awt;

import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;

public class Graphics {
	protected Graphics() {
	}
	
	public native Graphics create();
	native public Graphics create(int x, int y, int width, int height);
	public native void translate(int x, int y);
	public native Color getColor();
	public native void setColor(Color c);
	public native void setPaintMode();
	public native void setXORMode(Color c1);
	public native Font getFont();
	public native void setFont(Font font);
	public native FontMetrics getFontMetrics();
	public native FontMetrics getFontMetrics(Font f);
	public native Rectangle getClipBounds();
	public native void clipRect(int x, int y, int width, int height);
	public native void setClip(int x, int y, int width, int height);
	public native Shape getClip();
	public native void setClip(Shape clip);
	public native void copyArea(int x, int y, int width, int height, int dx, int dy);
	public native void drawLine(int x1, int y1, int x2, int y2);
	public native void fillRect(int x, int y, int width, int height);
	public native void drawRect(int x, int y, int width, int height);
	public native void clearRect(int x, int y, int width, int height);
	public native void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);
	public native void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight);
	public native void draw3DRect(int x, int y, int width, int height, boolean raised);
	public native void fill3DRect(int x, int y, int width, int height, boolean raised);
	public native void drawOval(int x, int y, int width, int height);
	public native void fillOval(int x, int y, int width, int height);
	public native void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle);
	public native void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle);
	native public void drawPolyline(int xPoints[], int yPoints[], int nPoints);
	native public void drawPolygon(int xPoints[], int yPoints[], int nPoints);
	native public void drawPolygon(Polygon p);
	public native void fillPolygon(int xPoints[], int yPoints[], int nPoints);
	public native void fillPolygon(Polygon p);
	public native void drawString(String str, int x, int y);
	public native void drawString(AttributedCharacterIterator iterator, int x, int y);
	native public void drawChars(char data[], int offset, int length, int x, int y);
	native public void drawBytes(byte data[], int offset, int length, int x, int y);
	public native boolean drawImage(Image img, int x, int y, ImageObserver observer);
	public native boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer);
	public native boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer);
	public native boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer);
	public native boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer);
	public native boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer);
	public native void dispose();
	public void finalize() {
		dispose();
	}
	public String toString() {
		return "Graphics";
	}
	@Deprecated native public Rectangle getClipRect();
	native public boolean hitClip(int x, int y, int width, int height);
	native public Rectangle getClipBounds(Rectangle r);
}
