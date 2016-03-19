package java.util.concurrent.locks;

import org.jetbrains.annotations.NotNull;

public class ReentrantReadWriteLock implements ReadWriteLock, java.io.Serializable {
	@NotNull
	@Override
	public Lock readLock() {
		return new ReentrantLock(); // Dummy LOCK! Since not implemented!
	}

	@NotNull
	@Override
	public Lock writeLock() {
		return new ReentrantLock(); // Dummy LOCK! Since not implemented!
	}
}
