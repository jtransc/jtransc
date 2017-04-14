package java.awt;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ImageCapabilities implements Cloneable {
	private boolean accelerated = false;

	public ImageCapabilities(boolean accelerated) {
		this.accelerated = accelerated;
	}

	public boolean isAccelerated() {
		return accelerated;
	}

	public boolean isTrueVolatile() {
		return false;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
