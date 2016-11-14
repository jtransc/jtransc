package java.awt;

public interface Transparency {
	public final static int OPAQUE = 1;
	public final static int BITMASK = 2;
	public final static int TRANSLUCENT = 3;

	public int getTransparency();
}
