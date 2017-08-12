package java.nio.channels;

public abstract class SelectionKey {
	public static final int OP_READ = 1;
	public static final int OP_WRITE = 4;
	public static final int OP_CONNECT = 8;
	public static final int OP_ACCEPT = 16;

	protected SelectionKey() {
	}

	public abstract SelectableChannel channel();

	public abstract Selector selector();

	public abstract boolean isValid();

	public abstract void cancel();

	public abstract int interestOps();

	public abstract SelectionKey interestOps(int ops);

	public abstract int readyOps();

	public final boolean isReadable() {
		return (readyOps() & OP_READ) != 0;
	}

	public final boolean isWritable() {
		return (readyOps() & OP_WRITE) != 0;
	}

	public final boolean isConnectable() {
		return (readyOps() & OP_CONNECT) != 0;
	}

	public final boolean isAcceptable() {
		return (readyOps() & OP_ACCEPT) != 0;
	}

	private Object attachment = null;

	synchronized public final Object attach(Object ob) {
		return attachment = ob;
	}

	synchronized public final Object attachment() {
		return attachment;
	}
}
