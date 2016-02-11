package jtransc.game.stage;

import jtransc.game.canvas.Context2D;

public class DisplayObject {
	private double x = 0.0;
	private double y = 0.0;
	private double scaleX = 1.0;
	private double scaleY = 1.0;
	private double rotation = 0.0;

	public double getRotationDegrees() {
		return Math.toDegrees(rotation);
	}

	public void setRotationDegrees(double value) {
		rotation = Math.toRadians(value);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getScaleX() {
		return scaleX;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public void render(Context2D ctx) {
		ctx.save();
		ctx.translate(x, y);
		ctx.scale(scaleX, scaleY);
		ctx.rotate(rotation);
		internalRender(ctx);
		ctx.restore();
	}

	public void internalRender(Context2D ctx) {
	}
}

