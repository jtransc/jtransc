package javax.sound.sampled;

@SuppressWarnings("WeakerAccess")
public class LineEvent extends java.util.EventObject {
	private final Type type;
	private final long position;

	public LineEvent(Line line, Type type, long position) {
		super(line);
		this.type = type;
		this.position = position;
	}

	public final Line getLine() {
		return (Line) getSource();
	}


	public final Type getType() {
		return type;
	}

	public final long getFramePosition() {
		return position;
	}

	@SuppressWarnings("WeakerAccess")
	public static class Type {
		private final String name;

		protected Type(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

		public static final Type OPEN = new Type("Open");
		public static final Type CLOSE = new Type("Close");
		public static final Type START = new Type("Start");
		public static final Type STOP = new Type("Stop");
	}

}
