package java.awt;

import java.awt.geom.Rectangle2D;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Rectangle extends Rectangle2D {
	public int x;
	public int y;
	public int width;
	public int height;

	public Rectangle() {
		this(0, 0, 0, 0);
	}

	public Rectangle(Rectangle r) {
		this(r.x, r.y, r.width, r.height);
	}

	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Rectangle(int width, int height) {
		this(0, 0, width, height);
	}

	public Rectangle(Point p, Dimension d) {
		this(p.x, p.y, d.width, d.height);
	}

	public Rectangle(Point p) {
		this(p.x, p.y, 0, 0);
	}

	public Rectangle(Dimension d) {
		this(0, 0, d.width, d.height);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}

	public Rectangle2D getBounds2D() {
		return new Rectangle(x, y, width, height);
	}

	public void setBounds(int x, int y, int width, int height) {
		reshape(x, y, width, height);
	}

	public void setRect(double x, double y, double width, double height) {
		reshape((int) x, (int) y, (int) width, (int) height);
	}

	public void reshape(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Point getLocation() {
		return new Point(x, y);
	}

	public void setLocation(Point p) {
		setLocation(p.x, p.y);
	}

	public void setLocation(int x, int y) {
		move(x, y);
	}

	public void move(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void translate(int dx, int dy) {
		this.move(this.x + dx, this.y + dy);
	}

	public Dimension getSize() {
		return new Dimension(width, height);
	}

	public void setSize(Dimension d) {
		setSize(d.width, d.height);
	}

	public void setSize(int width, int height) {
		resize(width, height);
	}

	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	public boolean contains(int x, int y) {
		return inside(x, y);
	}

	public boolean contains(Rectangle r) {
		return contains(r.x, r.y, r.width, r.height);
	}

	public boolean contains(int X, int Y, int W, int H) {
		throw new RuntimeException("not implemented");
	}

	public boolean inside(int X, int Y) {
		return X >= this.x && Y >= this.y && X < this.x + this.width && Y < this.y + this.height;
	}

	public boolean intersects(Rectangle r) {
		throw new RuntimeException("not implemented");
	}

	public Rectangle intersection(Rectangle r) {
		throw new RuntimeException("not implemented");
	}

	public Rectangle union(Rectangle r) {
		throw new RuntimeException("not implemented");
	}

	public void add(int newx, int newy) {
		throw new RuntimeException("not implemented");
	}

	public void add(Point pt) {
		add(pt.x, pt.y);
	}

	public void add(Rectangle r) {
		throw new RuntimeException("not implemented");
	}

	public void grow(int h, int v) {
		throw new RuntimeException("not implemented");
	}

	public boolean isEmpty() {
		return (width <= 0) || (height <= 0);
	}

	public int outcode(double x, double y) {
		throw new RuntimeException("not implemented");
	}

	public Rectangle2D createIntersection(Rectangle2D r) {
		throw new RuntimeException("not implemented");
	}

	public Rectangle2D createUnion(Rectangle2D r) {
		throw new RuntimeException("not implemented");
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof Rectangle) {
			Rectangle that = (Rectangle) obj;
			return ((this.x == that.x) && (this.y == that.y) && (this.width == that.width) && (this.height == that.height));
		}
		return false;
	}

	public String toString() {
		return "Rectangle[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
	}
}