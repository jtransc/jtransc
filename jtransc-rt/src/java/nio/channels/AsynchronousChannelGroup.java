package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class AsynchronousChannelGroup {
	protected AsynchronousChannelGroup(AsynchronousChannelProvider provider) {
	}

	native public final AsynchronousChannelProvider provider();

	native public static AsynchronousChannelGroup withFixedThreadPool(int nThreads, ThreadFactory threadFactory) throws IOException;

	native public static AsynchronousChannelGroup withCachedThreadPool(ExecutorService executor, int initialSize) throws IOException;

	native public static AsynchronousChannelGroup withThreadPool(ExecutorService executor) throws IOException;

	native public boolean isShutdown();

	native public boolean isTerminated();

	native public void shutdown();

	native public void shutdownNow() throws IOException;

	native public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
}
