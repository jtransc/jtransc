package java.lang.management;

public interface ThreadMXBean extends PlatformManagedObject {
	int getThreadCount();

	int getPeakThreadCount();

	long getTotalStartedThreadCount();

	int getDaemonThreadCount();

	long[] getAllThreadIds();

	ThreadInfo getThreadInfo(long id);

	ThreadInfo[] getThreadInfo(long[] ids);

	ThreadInfo getThreadInfo(long id, int maxDepth);

	ThreadInfo[] getThreadInfo(long[] ids, int maxDepth);

	boolean isThreadContentionMonitoringSupported();

	boolean isThreadContentionMonitoringEnabled();

	void setThreadContentionMonitoringEnabled(boolean enable);

	long getCurrentThreadCpuTime();

	long getCurrentThreadUserTime();

	long getThreadCpuTime(long id);

	long getThreadUserTime(long id);

	boolean isThreadCpuTimeSupported();

	boolean isCurrentThreadCpuTimeSupported();

	boolean isThreadCpuTimeEnabled();

	void setThreadCpuTimeEnabled(boolean enable);

	long[] findMonitorDeadlockedThreads();

	void resetPeakThreadCount();

	long[] findDeadlockedThreads();

	boolean isObjectMonitorUsageSupported();

	boolean isSynchronizerUsageSupported();

	ThreadInfo[] getThreadInfo(long[] ids, boolean lockedMonitors, boolean lockedSynchronizers);

	ThreadInfo[] dumpAllThreads(boolean lockedMonitors, boolean lockedSynchronizers);
}
