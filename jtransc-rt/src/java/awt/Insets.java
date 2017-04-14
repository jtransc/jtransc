package java.awt;

@SuppressWarnings("WeakerAccess")
public class Insets implements Cloneable, java.io.Serializable {
	public int top;
	public int left;
	public int bottom;
	public int right;

	public Insets(int top, int left, int bottom, int right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	public void set(int top, int left, int bottom, int right) {
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Insets) {
			Insets that = (Insets) obj;
			return ((this.top == that.top) && (this.left == that.left) && (this.bottom == that.bottom) && (this.right == that.right));
		}
		return false;
	}

	public int hashCode() {
		return left + bottom + right + top;
	}

	public String toString() {
		return "Insets[top=" + top + ",left=" + left + ",bottom=" + bottom + ",right=" + right + "]";
	}
}
