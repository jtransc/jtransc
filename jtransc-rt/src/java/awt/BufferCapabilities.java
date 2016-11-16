package java.awt;

@SuppressWarnings({"WeakerAccess", "unused"})
public class BufferCapabilities implements Cloneable {
	private ImageCapabilities frontCaps;
	private ImageCapabilities backCaps;
	private FlipContents flipContents;

	public BufferCapabilities(ImageCapabilities frontCaps, ImageCapabilities backCaps, FlipContents flipContents) {
		this.frontCaps = frontCaps;
		this.backCaps = backCaps;
		this.flipContents = flipContents;
	}

	public ImageCapabilities getFrontBufferCapabilities() {
		return frontCaps;
	}

	public ImageCapabilities getBackBufferCapabilities() {
		return backCaps;
	}

	public boolean isPageFlipping() {
		return (getFlipContents() != null);
	}

	public FlipContents getFlipContents() {
		return flipContents;
	}

	public boolean isFullScreenRequired() {
		return false;
	}

	public boolean isMultiBufferAvailable() {
		return false;
	}

	public static final class FlipContents {
		public static final FlipContents UNDEFINED = new FlipContents();
		public static final FlipContents BACKGROUND = new FlipContents();
		public static final FlipContents PRIOR = new FlipContents();
		public static final FlipContents COPIED = new FlipContents();

		private FlipContents() {
		}
	}
}
