package jtransc.game.math;

public class Matrix {
	static private final Matrix temp = new Matrix();

	public double a = 1;
	public double b = 0;
	public double c = 0;
	public double d = 1;
	public double tx = 0;
	public double ty = 0;

	public Matrix() {
		this(1, 0, 0, 1, 0, 0);
	}

	public Matrix(double a, double b, double c, double d, double tx, double ty) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.tx = tx;
		this.ty = ty;
	}

	public Matrix identity() {
		return setTo(1.0, 0.0, 0.0, 1.0, 0.0, 0.0);
	}

	public boolean isIdentity() {
		return (a == 1.0) && (b == 0.0) && (c == 0.0) && (d == 1.0) && (tx == 0.0) && (ty == 0.0);
	}

	public Matrix rotate(double theta) {
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);

		double a1 = a * cos - b * sin;
		b = a * sin + b * cos;
		a = a1;

		double c1 = c * cos - d * sin;
		d = c * sin + d * cos;
		c = c1;

		double tx1 = tx * cos - ty * sin;
		ty = tx * sin + ty * cos;
		tx = tx1;

		return this;
	}

	public Matrix translate(double dx, double dy) {
		tx += dx;
		ty += dy;
		return this;
	}

	public Matrix pretranslate(double dx, double dy) {
		tx += a * dx + c * dy;
		ty += b * dx + d * dy;
		return this;
	}

	public Matrix clone() {
		return new Matrix(a, b, c, d, tx, ty);
	}

	public Matrix copyFrom(Matrix that) {
		return setTo(that.a, that.b, that.c, that.d, that.tx, that.ty);
	}

	public Matrix invert() {
		double norm = a * d - b * c;

		if (norm == 0.0) {
			a = 0.0;
			b = 0.0;
			c = 0.0;
			d = 0.0;
			tx = -tx;
			ty = -ty;
		} else {
			norm = 1.0 / norm;
			double a1 = d * norm;
			d = a * norm;
			a = a1;
			b *= -norm;
			c *= -norm;

			double tx1 = -a * tx - c * ty;
			ty = -b * tx - d * ty;
			tx = tx1;
		}

		//checkProperties()

		return this;
	}

	public Matrix setTo(double a, double b, double c, double d, double tx, double ty) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.tx = tx;
		this.ty = ty;
		return this;
	}

	public Matrix scale(double sx, double sy) {
		return setTo(a * sx, b * sx, c * sy, d * sy, tx * sx, ty * sy);
	}

	public Matrix prescale(double sx, double sy) {
		return setTo(a * sx, b * sx, c * sy, d * sy, tx, ty);
	}

	public Matrix prerotate(double angle) {
		final double sin = Math.sin(angle);
		final double cos = Math.cos(angle);

		return setTo(
			a * cos + c * sin, b * cos + d * sin,
			c * cos - a * sin, d * cos - b * sin,
			tx, ty
		);
	}

	public Matrix preskew(double skewX, double skewY) {
		double sinX = Math.sin(skewX);
		double cosX = Math.cos(skewX);
		double sinY = Math.sin(skewY);
		double cosY = Math.cos(skewY);

		return setTo(
			a * cosY + c * sinY,
			b * cosY + d * sinY,
			c * cosX - a * sinX,
			d * cosX - b * sinX,
			tx, ty
		);
	}

	public Matrix skew(double skewX, double skewY) {
		double sinX = Math.sin(skewX);
		double cosX = Math.cos(skewX);
		double sinY = Math.sin(skewY);
		double cosY = Math.cos(skewY);

		return setTo(
			a * cosY - b * sinX,
			a * sinY + b * cosX,
			c * cosY - d * sinX,
			c * sinY + d * cosX,
			tx * cosY - ty * sinX,
			tx * sinY + ty * cosX
		);
	}

	public Matrix multiply(Matrix l, Matrix r) {
		return setTo(
			l.a * r.a + l.b * r.c,
			l.a * r.b + l.b * r.d,
			l.c * r.a + l.d * r.c,
			l.c * r.b + l.d * r.d,
			l.tx * r.a + l.ty * r.c + r.tx,
			l.tx * r.b + l.ty * r.d + r.ty
		);
	}

	public Matrix concat(Matrix that) {
		return multiply(this, that);
	}

	public Matrix preconcat(Matrix that) {
		return multiply(that, this);
	}

	public Matrix pretransform(double a, double b, double c, double d, double tx, double ty) {
		return preconcat(temp.setTo(a, b, c, d, tx, ty));
	}

	public Point transform(double px, double py) {
		return transform(px, py, new Point());
	}

	public Point transform(double px, double py, Point result) {
		return result.setTo(transformX(px, py), transformY(px, py));
	}

    public double transformX(double px, double py) {
        return this.a * px + this.c * py + this.tx;
    }

    public double transformY(double px, double py) {
        return this.d * py + this.b * px + this.ty;
    }

    public Point transform(Point point, Point result) {
        return transform(point.x, point.y, result);
    }

    public Matrix setTransform(double x, double y, double scaleX, double scaleY, double rotation, double skewX, double skewY) {
        if (skewX == 0.0 && skewY == 0.0) {
            if (rotation == 0.0) {
                this.setTo(scaleX, 0.0, 0.0, scaleY, x, y);
            } else {
                double cos = Math.cos(rotation);
                double sin = Math.sin(rotation);
                this.setTo(cos * scaleX, sin * scaleY, -sin * scaleX, cos * scaleY, x, y);
            }
        } else {
            identity();
            scale(scaleX, scaleY);
            skew(skewX, skewY);
            rotate(rotation);
            translate(x, y);
        }
        return this;
    }
}