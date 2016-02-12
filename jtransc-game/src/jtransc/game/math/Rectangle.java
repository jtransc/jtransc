package jtransc.game.math;

public class Rectangle {
	private double left;
	private double top;
	private double right;
	private double bottom;

	public Rectangle(double x, double y, double width, double height) {
		this.setToSize(x, y, width, height);
	}

	public Rectangle() {
		this._setToBounds(0, 0, 0, 0);
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public void setTop(double top) {
		this.top = top;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public double getLeft() {

		return left;
	}

	public double getTop() {
		return top;
	}

	public double getRight() {
		return right;
	}

	public double getBottom() {
		return bottom;
	}

	public double getWidth() {
		return right - left;
	}

	public double getHeight() {
		return bottom - top;
	}

	public void setWidth(double width) {
		right = left + width;
	}

	public void setHeight(double height) {
		right = left + height;
	}

	private Rectangle _setToBounds(double left, double top, double right, double bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		return this;
	}

	public Rectangle setToBounds(double left, double top, double right, double bottom) {
		return _setToBounds(left, top, right, bottom);
	}

	public Rectangle setToSize(double x, double y, double width, double height) {
		return _setToBounds(x, y, x + width, y + height);
	}

	public Rectangle copyFrom(Rectangle that) {
		return setToBounds(that.left, that.top, that.right, that.bottom);
	}

    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }

    public boolean contains(double x, double y) {
        return x >= this.left && y >= this.top && x < this.right && y < this.bottom;
    }
}
