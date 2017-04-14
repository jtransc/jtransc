package java.awt;

@SuppressWarnings("WeakerAccess")
public class Dimension {
	public int width;
	public int height;

	public Dimension() {
		this(0, 0);
	}

	public Dimension(Dimension d) {
		this(d.width, d.height);
	}

	public Dimension(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public void setSize(double width, double height) {
		this.width = (int) Math.ceil(width);
		this.height = (int) Math.ceil(height);
	}

	public Dimension getSize() {
		return new Dimension(width, height);
	}

	public void setSize(Dimension d) {
		setSize(d.width, d.height);
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof Dimension) {
			Dimension that = (Dimension) obj;
			return (this.width == that.width) && (this.height == that.height);
		}
		return false;
	}

	public int hashCode() {
		return width + height << 16;
	}

	public String toString() {
		return "Dimension[width=" + width + ",height=" + height + "]";
	}
}
