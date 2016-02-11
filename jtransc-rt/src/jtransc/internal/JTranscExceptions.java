package jtransc.internal;

public class JTranscExceptions {
	static public void invalidIndex() {
		throw new IllegalArgumentException("Invalid index");
	}

	public static void illegalState() {
		throw new IllegalStateException();
	}
}
