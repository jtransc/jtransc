package java.lang.management;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import java.util.List;
import java.util.Set;

public class ManagementFactory {
	private ManagementFactory() {
	}

	public final static String CLASS_LOADING_MXBEAN_NAME = "java.lang:type=ClassLoading";
	public final static String COMPILATION_MXBEAN_NAME = "java.lang:type=Compilation";
	public final static String MEMORY_MXBEAN_NAME = "java.lang:type=Memory";
	public final static String OPERATING_SYSTEM_MXBEAN_NAME = "java.lang:type=OperatingSystem";
	public final static String RUNTIME_MXBEAN_NAME = "java.lang:type=Runtime";
	public final static String THREAD_MXBEAN_NAME = "java.lang:type=Threading";
	public final static String GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE = "java.lang:type=GarbageCollector";
	public final static String MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE = "java.lang:type=MemoryManager";
	public final static String MEMORY_POOL_MXBEAN_DOMAIN_TYPE = "java.lang:type=MemoryPool";

	native public static ThreadMXBean getThreadMXBean();

	/*
	native public static ClassLoadingMXBean getClassLoadingMXBean();

	native public static MemoryMXBean getMemoryMXBean();

	native public static RuntimeMXBean getRuntimeMXBean();

	native public static CompilationMXBean getCompilationMXBean();

	native public static OperatingSystemMXBean getOperatingSystemMXBean();

	native public static List<MemoryPoolMXBean> getMemoryPoolMXBeans();

	native public static List<MemoryManagerMXBean> getMemoryManagerMXBeans();

	native public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans();

	native public static synchronized MBeanServer getPlatformMBeanServer();

	native public static <T> T newPlatformMXBeanProxy(MBeanServerConnection connection, String mxbeanName, Class<T> mxbeanInterface) throws java.io.IOException;

	native public static <T extends PlatformManagedObject> T getPlatformMXBean(Class<T> mxbeanInterface);

	native public static <T extends PlatformManagedObject> List<T> getPlatformMXBeans(Class<T> mxbeanInterface);

	native public static <T extends PlatformManagedObject> T getPlatformMXBean(MBeanServerConnection connection, Class<T> mxbeanInterface) throws java.io.IOException;

	native public static <T extends PlatformManagedObject> List<T> getPlatformMXBeans(MBeanServerConnection connection, Class<T> mxbeanInterface) throws java.io.IOException;

	native public static Set<Class<? extends PlatformManagedObject>> getPlatformManagementInterfaces();
	*/
}
