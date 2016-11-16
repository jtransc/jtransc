package javax.sound.sampled;

@SuppressWarnings("WeakerAccess")
public interface LineListener extends java.util.EventListener {
	void update(LineEvent event);
}
