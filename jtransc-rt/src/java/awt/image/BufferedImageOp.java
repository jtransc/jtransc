package java.awt.image;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public interface BufferedImageOp {
	public BufferedImage filter(BufferedImage src, BufferedImage dest);

	public Rectangle2D getBounds2D(BufferedImage src);

	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM);

	public Point2D getPoint2D(Point2D srcPt, Point2D dstPt);

	public RenderingHints getRenderingHints();
}
