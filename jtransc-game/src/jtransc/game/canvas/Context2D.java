package jtransc.game.canvas;

import jtransc.game.batch.BatchBuilder;
import jtransc.game.math.Matrix;
import jtransc.game.math.Point;
import jtransc.game.util.StackPool;
import jtransc.game.util.StackPoolGenerator;

public class Context2D {
	final Canvas canvas;
	private Matrix matrix = new Matrix();
	private StackPool<Matrix> stack = new StackPool<Matrix>(128, new StackPoolGenerator<Matrix>() {
		@Override
		public Matrix generate(int index) {
			return new Matrix();
		}
	});
	private BatchBuilder batches;
	private double _globalAlpha = 1.0;

	public Context2D(Canvas canvas) {
		this.canvas = canvas;
		this.batches = canvas.batches;
	}

	public double getGlobalAlpha() {
		return _globalAlpha;
	}

	public void setGlobalAlpha(double value) {
		_globalAlpha = value;
	}

	public void reset() {
		_globalAlpha = 1.0;
		matrix.identity();
		stack.clear();
		batches.reset();
	}

	public void save() {
		stack.push().copyFrom(matrix);
	}

	public void restore() {
		matrix.copyFrom(stack.pop());
	}

	public Context2D translate(double dx, double dy) {
		matrix.pretranslate(dx, dy);
		return this;
	}

	public Context2D scale(double s) {
		return scale(s, s);
	}

	public Context2D scale(double sx, double sy) {
		matrix.prescale(sx, sy);
		return this;
	}

	public Context2D rotate(double angle) {
		matrix.prerotate(angle);
		return this;
	}

	public Context2D transform(double a, double b, double c, double d, double tx, double ty) {
		matrix.pretransform(a, b, c, d, tx, ty);
		return this;
	}

	public Context2D setTransform(double a, double b, double c, double d, double tx, double ty) {
		matrix.setTo(a, b, c, d, tx, ty);
		return this;
	}

	private final Point p0 = new Point();
	private final Point p1 = new Point();
	private final Point p2 = new Point();
	private final Point p3 = new Point();

	public void fillRect(double x, double y, double w, double h) {
		matrix.transform(x + 0, y + 0, p0);
		matrix.transform(x + w, y + 0, p1);
		matrix.transform(x + 0, y + h, p2);
		matrix.transform(x + w, y + h, p3);

		batches.quad(0, p0, p1, p2, p3, 0f, 0f, 1f, 1f, -1, 0x7f7f7f7f);
	}

	public void drawImage(Texture image) {
		drawImage(image, 0, 0, image.width, image.height);
	}

	public void drawImage(Texture image, double x, double y) {
		drawImage(image, x, y, image.width, image.height);
	}

	public void drawImage(Texture image, double x, double y, double w, double h) {
		final Texture i = image;

		matrix.transform(x + 0, y + 0, p0);
		matrix.transform(x + w, y + 0, p1);
		matrix.transform(x + 0, y + h, p2);
		matrix.transform(x + w, y + h, p3);

		batches.quad(i.base.id, p0, p1, p2, p3, i.tx0, i.ty0, i.tx1, i.ty1, -1, 0x7f7f7f7f);
	}
}
