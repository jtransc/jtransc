package java.awt;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public abstract class Graphics2D extends Graphics {
	protected Graphics2D() {
	}

	public native void draw3DRect(int x, int y, int width, int height, boolean raised);

	public native void fill3DRect(int x, int y, int width, int height, boolean raised);

	public native void draw(Shape s);

	public native boolean drawImage(Image img, AffineTransform xform, ImageObserver obs);

	public native void drawImage(BufferedImage img, BufferedImageOp op, int x, int y);

	public native void drawRenderedImage(RenderedImage img, AffineTransform xform);

	public native void drawRenderableImage(RenderableImage img, AffineTransform xform);

	public native void drawString(String str, int x, int y);

	public native void drawString(String str, float x, float y);

	public native void drawString(AttributedCharacterIterator iterator, int x, int y);

	public native void drawString(AttributedCharacterIterator iterator, float x, float y);

	public native void drawGlyphVector(GlyphVector g, float x, float y);

	public native void fill(Shape s);

	public native boolean hit(Rectangle rect, Shape s, boolean onStroke);

	public native GraphicsConfiguration getDeviceConfiguration();

	public native void setComposite(Composite comp);

	public native void setPaint(Paint paint);

	public native void setStroke(Stroke s);

	public native void setRenderingHint(RenderingHints.Key hintKey, Object hintValue);

	public native Object getRenderingHint(RenderingHints.Key hintKey);

	public native void setRenderingHints(Map<?, ?> hints);

	public native void addRenderingHints(Map<?, ?> hints);

	public native RenderingHints getRenderingHints();

	public native void translate(int x, int y);

	public native void translate(double tx, double ty);

	public native void rotate(double theta);

	public native void rotate(double theta, double x, double y);

	public native void scale(double sx, double sy);

	public native void shear(double shx, double shy);

	public native void transform(AffineTransform Tx);

	public native void setTransform(AffineTransform Tx);

	public native AffineTransform getTransform();

	public native Paint getPaint();

	public native Composite getComposite();

	public native void setBackground(Color color);

	public native Color getBackground();

	public native Stroke getStroke();

	public native void clip(Shape s);

	public native FontRenderContext getFontRenderContext();
}
