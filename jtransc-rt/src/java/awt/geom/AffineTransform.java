package java.awt.geom;

import java.awt.*;

public class AffineTransform {
	public static final int TYPE_IDENTITY = 0;
	public static final int TYPE_TRANSLATION = 1;
	public static final int TYPE_UNIFORM_SCALE = 2;
	public static final int TYPE_GENERAL_SCALE = 4;
	public static final int TYPE_MASK_SCALE = (TYPE_UNIFORM_SCALE | TYPE_GENERAL_SCALE);
	public static final int TYPE_FLIP = 64;
	public static final int TYPE_QUADRANT_ROTATION = 8;
	public static final int TYPE_GENERAL_ROTATION = 16;
	public static final int TYPE_MASK_ROTATION = (TYPE_QUADRANT_ROTATION | TYPE_GENERAL_ROTATION);
	public static final int TYPE_GENERAL_TRANSFORM = 32;
	private double a, b, c, d, tx, ty;
	private transient int type;

	public AffineTransform() {
		a = d = 1.0;
	}

	public AffineTransform(AffineTransform that) {
		this.a = that.a;
		this.b = that.b;
		this.c = that.c;
		this.d = that.d;
		this.tx = that.tx;
		this.ty = that.ty;
		this.type = that.type;
	}

	public AffineTransform(float a, float b, float c, float d, float tx, float ty) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.tx = tx;
		this.ty = ty;
	}

	public AffineTransform(float[] v) {
		a = v[0];
		b = v[1];
		c = v[2];
		d = v[3];
		if (v.length > 5) {
			tx = v[4];
			ty = v[5];
		}
	}

	public AffineTransform(double a, double b, double c, double d, double tx, double ty) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.tx = tx;
		this.ty = ty;
	}

	public AffineTransform(double[] v) {
		a = v[0];
		b = v[1];
		c = v[2];
		d = v[3];
		if (v.length > 5) {
			tx = v[4];
			ty = v[5];
		}
	}

	public native static AffineTransform getTranslateInstance(double tx, double ty);

	public native static AffineTransform getRotateInstance(double theta);

	public native static AffineTransform getRotateInstance(double theta, double anchorx, double anchory);

	native public static AffineTransform getRotateInstance(double vecx, double vecy);

	native public static AffineTransform getRotateInstance(double vecx, double vecy, double anchorx, double anchory);

	native public static AffineTransform getQuadrantRotateInstance(int numquadrants);

	native public static AffineTransform getQuadrantRotateInstance(int numquadrants, double anchorx, double anchory);

	native public static AffineTransform getScaleInstance(double sx, double sy);

	native public static AffineTransform getShearInstance(double shx, double shy);

	public int getType() {
		return TYPE_GENERAL_TRANSFORM;
	}

	native public double getDeterminant();

	native public void getMatrix(double[] flatmatrix);

	public double getScaleX() {
		return a;
	}

	public double getScaleY() {
		return d;
	}

	public double getShearX() {
		return c;
	}

	public double getShearY() {
		return b;
	}

	public double getTranslateX() {
		return tx;
	}

	public double getTranslateY() {
		return ty;
	}

	native public void translate(double tx, double ty);

	native public void rotate(double theta);

	native public void rotate(double theta, double anchorx, double anchory);

	native public void rotate(double vecx, double vecy);

	native public void rotate(double vecx, double vecy, double anchorx, double anchory);

	native public void quadrantRotate(int numquadrants);

	native public void quadrantRotate(int numquadrants, double anchorx, double anchory);

	native public void scale(double sx, double sy);

	native public void shear(double shx, double shy);

	native public void setToIdentity();

	native public void setToTranslation(double tx, double ty);

	native public void setToRotation(double theta);

	native public void setToRotation(double theta, double anchorx, double anchory);

	native public void setToRotation(double vecx, double vecy);

	native public void setToRotation(double vecx, double vecy, double anchorx, double anchory);

	native public void setToQuadrantRotation(int numquadrants);

	native public void setToQuadrantRotation(int numquadrants, double anchorx, double anchory);

	native public void setToScale(double sx, double sy);

	native public void setToShear(double shx, double shy);

	native public void setTransform(AffineTransform Tx);

	native public void setTransform(double m00, double m10, double m01, double m11, double m02, double m12);

	native public void concatenate(AffineTransform Tx);

	native public void preConcatenate(AffineTransform Tx);

	native public AffineTransform createInverse() throws NoninvertibleTransformException;

	native public void invert() throws NoninvertibleTransformException;

	native public Point2D transform(Point2D ptSrc, Point2D ptDst);

	native public void transform(Point2D[] ptSrc, int srcOff, Point2D[] ptDst, int dstOff, int numPts);

	native public void transform(float[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts);

	native public void transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts);

	native public void transform(float[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts);

	native public void transform(double[] srcPts, int srcOff, float[] dstPts, int dstOff, int numPts);

	native public Point2D inverseTransform(Point2D ptSrc, Point2D ptDst) throws NoninvertibleTransformException;

	native public void inverseTransform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts) throws NoninvertibleTransformException;

	native public Point2D deltaTransform(Point2D ptSrc, Point2D ptDst);

	native public void deltaTransform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, int numPts);

	native public Shape createTransformedShape(Shape pSrc);

	native public boolean isIdentity();

	public String toString() {
		return "AffineTransform";
	}
}
