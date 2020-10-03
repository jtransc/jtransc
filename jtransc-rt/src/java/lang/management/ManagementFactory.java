package java.lang.management;

public class ManagementFactory {
	private static ThreadMXBean _current;

	static ThreadMXBean	getThreadMXBean() {
		if (_current == null) _current = new DefaultThreadMXBean();
		return _current;
	}

	static class DefaultThreadMXBean implements ThreadMXBean {
		public int getThreadCount() { return 0; }
		public int getPeakThreadCount() { return 0; }
		public long getTotalStartedThreadCount() { return 0L; }
		public int getDaemonThreadCount() { return 0; }
		public long[] getAllThreadIds() { return new long[0]; }
		public boolean isThreadContentionMonitoringSupported() { return false; }
		public boolean isThreadContentionMonitoringEnabled() { return false; }
		public void setThreadContentionMonitoringEnabled(boolean enable) { }
		public long getCurrentThreadCpuTime() { return 0L; }
		public long getCurrentThreadUserTime() { return 0L; }
		public long getThreadCpuTime(long id) { return 0L; }
		public long getThreadUserTime(long id) { return 0L; }
		public boolean isThreadCpuTimeSupported() { return false; }
		public boolean isCurrentThreadCpuTimeSupported() { return false; }
		public boolean isThreadCpuTimeEnabled() { return false; }
		public void setThreadCpuTimeEnabled(boolean enable) { }
		public long[] findMonitorDeadlockedThreads() { return new long[0]; }
		public void resetPeakThreadCount() { }
		public long[] findDeadlockedThreads() { return new long[0]; }
		public boolean isObjectMonitorUsageSupported() { return false; }
		public boolean isSynchronizerUsageSupported() { return false; }
	}
}
