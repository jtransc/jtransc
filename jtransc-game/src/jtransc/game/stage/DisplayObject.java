package jtransc.game.stage;

import jtransc.game.canvas.Context2D;
import jtransc.game.event.EventDispatcher;
import jtransc.game.util.Signal;

public class DisplayObject extends EventDispatcher {
	private double x = 0.0;
	private double y = 0.0;
	private double scaleX = 1.0;
	private double scaleY = 1.0;
	private double rotation = 0.0;
	private double speed = 1.0;
    public Signal<Integer> onUpdate = new Signal<Integer>();

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	private double alpha = 1.0;

	final public void update(int dtMs) {
		dtMs = (int) (dtMs * speed);
		onUpdate.dispatch(dtMs);
		internalUpdate(dtMs);
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

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

	final public void render(Context2D ctx) {
		double oldAlpha = ctx.getGlobalAlpha();
		ctx.setGlobalAlpha(ctx.getGlobalAlpha() * this.alpha);
		ctx.save();
		ctx.translate(x, y);
		ctx.scale(scaleX, scaleY);
		ctx.rotate(rotation);
		internalRender(ctx);
		ctx.restore();
		ctx.setGlobalAlpha(oldAlpha);
	}

	public void internalRender(Context2D ctx) {
	}

	public void internalUpdate(int dtMs) {
	}
}

