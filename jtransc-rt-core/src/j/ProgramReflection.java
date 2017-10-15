package j;

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.ds.FastIntMap;
import com.jtransc.ds.FastStringMap;
import com.jtransc.io.JTranscConsole;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * MetaReflectionPlugin set those methods
 */
@SuppressWarnings({"unused", "WeakerAccess", "ConstantConditions"})
public class ProgramReflection {

	// Static initialization for classes.
	// Necessary due to existing getClass() method in code will generate static constructor.
	//static {
	//	_ensure();
	//}


	static public ClassInfo[] _classInfos;
	static public String[] _classNames;
	static public FastStringMap<ClassInfo> _classInfosByName;
	private static final FastIntMap<FastIntMap<MemberInfo>> _directMethodsInfo = new FastIntMap<FastIntMap<MemberInfo>>();
	private static boolean _ensureMethodsDone = false;

	@SuppressWarnings("ConstantConditions")
	@JTranscSync
	static public void _ensure() {
		if (_classInfos != null) return;

		_classInfosByName = new FastStringMap<ClassInfo>();
		_classInfos = ProgramReflection.getAllClasses();

		if(_classInfos != null) {
			_classNames = new String[_classInfos.length];

			for (ClassInfo info : _classInfos) {
				if (info == null) continue;
				//if (info.name == null) JTranscConsole.error("ProgramReflection.ensure: info.name==null");
				_classInfosByName.set(info.name, info);
				_classNames[info.id] = info.name;
			}
		}
	}

	@JTranscSync
	static public void _ensureMethods() {
		if (_ensureMethodsDone) return;
		_ensure();

		for (ClassInfo info : _classInfos) {
			if (info == null) continue;
			FastIntMap<MemberInfo> map = new FastIntMap<MemberInfo>();
			_directMethodsInfo.set(info.id, map);
			MemberInfo[] minfo = getMethods(info.id);
			if (minfo != null) {
				for (MemberInfo i : minfo) {
					map.set(i.id, i);
				}
			}
		}
		_ensureMethodsDone = true;
	}

	@JTranscSync
	static public boolean hasClassWithName(String name) {
		ProgramReflection._ensure();
		return ProgramReflection._classInfosByName.has(name);
	}

	@JTranscSync
	static public ClassInfo getClassInfoWithName(String name) {
		ProgramReflection._ensure();
		return hasClassWithName(name) ? ProgramReflection._classInfosByName.get(name) : null;
	}

	// Class
	@JTranscSync
	static public ClassInfo[] getAllClasses() {
		return _classInfos != null ? _classInfos : AllClasses.getAllClasses();
	}

	public static class AllClasses {
		public ClassInfo[] test;

		@JTranscSync
		static public ClassInfo[] getAllClasses() {
			ClassInfo[][] classInfoParts = new ClassInfo[6][];
			classInfoParts[0] = getAllClasses3000();
			classInfoParts[1] = getAllClasses6000();
			classInfoParts[2] = getAllClasses9000();
			classInfoParts[3] = getAllClasses12000();
			classInfoParts[4] = getAllClasses15000();
			classInfoParts[5] = getAllClassesMax();
			ClassInfo[] classInfo = new ClassInfo[getAllClassesCount()];
			for (ClassInfo[] classInfoPart : classInfoParts) {
				for (ClassInfo aClassInfo : classInfoPart) {
					classInfo[aClassInfo.id] = aClassInfo;
				}
			}
			return classInfo;
		}

		@JTranscSync
		static public int getAllClassesCount() {
			return 0;
		}

		@JTranscSync
		static public ClassInfo[] getAllClasses3000() {
			return new ClassInfo[0];
		}

		@JTranscSync
		static public ClassInfo[] getAllClasses6000() {
			return new ClassInfo[0];
		}

		@JTranscSync
		static public ClassInfo[] getAllClasses9000() {
			return new ClassInfo[0];
		}

		@JTranscSync
		static public ClassInfo[] getAllClasses12000() {
			return new ClassInfo[0];
		}

		@JTranscSync
		static public ClassInfo[] getAllClasses15000() {
			return new ClassInfo[0];
		}

		@JTranscSync
		static public ClassInfo[] getAllClassesMax() {
			return new ClassInfo[0];
		}
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	@JTranscSync
	static public MemberInfo[] getConstructors(int classId) {
		return AllConstructors.getConstructors(classId);
	}

	public static class AllConstructors {
		@JTranscSync
		static public MemberInfo[] getConstructors(int classId) {
			MemberInfo[] ret = AllConstructorsFirst.getConstructors(classId);
			if (ret != null && ret.length > 0) {
				return ret;
			}
			ret = AllConstructorsMiddle.getConstructors(classId);
			if (ret != null && ret.length > 0) {
				return ret;
			}
			return AllConstructorsLast.getConstructors(classId);
		}
	}

	public static class AllConstructorsFirst {
		@JTranscSync
		static public MemberInfo[] getConstructors(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllConstructorsMiddle {
		@JTranscSync
		static public MemberInfo[] getConstructors(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllConstructorsLast {
		@JTranscSync
		static public MemberInfo[] getConstructors(int classId) {
			return new MemberInfo[0];
		}
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	@JTranscSync
	static public MemberInfo[] getMethods(int classId) {
		return AllMethods.getMethods(classId);
	}

	public static class AllMethods {
		@JTranscSync
		public AllMethods() {}

		@JTranscSync
		static public MemberInfo[] getMethods(int classId) {
			MemberInfo[] ret = AllMethodsFirst.getMethods(classId);
			if (ret != null && ret.length > 0) {
				return ret;
			}
			ret = AllMethodsMiddle.getMethods(classId);
			if (ret != null && ret.length > 0) {
				return ret;
			}
			return AllMethodsLast.getMethods(classId);
		}
	}

	public static class AllMethodsFirst {
		@JTranscSync
		public AllMethodsFirst() {}

		@JTranscSync
		static public MemberInfo[] getMethods(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllMethodsMiddle {
		@JTranscSync
		public AllMethodsMiddle() {}

		@JTranscSync
		static public MemberInfo[] getMethods(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllMethodsLast {
		@JTranscSync
		public AllMethodsLast() {}

		@JTranscSync
		static public MemberInfo[] getMethods(int classId) {
			return new MemberInfo[0];
		}
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	@JTranscSync
	static public MemberInfo[] getFields(int classId) {
		return AllFields.getFields(classId);
	}

	public static class AllFields {
		@JTranscSync
		public AllFields() {}

		@JTranscSync
		static public MemberInfo[] getFields(int classId) {
			MemberInfo[] ret = AllFieldsFirst.getFields(classId);
			if (ret != null && ret.length > 0) {
				return ret;
			}
			ret = AllFieldsMiddle.getFields(classId);
			if (ret != null && ret.length > 0) {
				return ret;
			}
			return AllFieldsLast.getFields(classId);
		}
	}

	public static class AllFieldsFirst {
		@JTranscSync
		public AllFieldsFirst() {}

		@JTranscSync
		static public MemberInfo[] getFields(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllFieldsMiddle {
		@JTranscSync
		public AllFieldsMiddle() {}

		@JTranscSync
		static public MemberInfo[] getFields(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllFieldsLast {
		@JTranscSync
		public AllFieldsLast() {}

		@JTranscSync
		static public MemberInfo[] getFields(int classId) {
			return new MemberInfo[0];
		}
	}

	// Constructor
	@JTranscAsync
	static public Object dynamicNew(int classId, int constructorId, Object[] params) {
		return DynamicNew.dynamicNew(classId, constructorId, params);
	}

	// Method
	@JTranscAsync
	static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
		return DynamicInvoke.dynamicInvoke(classId, methodId, object, params);
	}

	// Field
	@JTranscSync
	static public Object dynamicGet(int classId, int fieldId, Object object) {
		return DynamicGet.dynamicGet(classId, fieldId, object);
	}

	@JTranscSync
	static public void dynamicSet(int classId, int fieldId, Object object, Object value) {
		DynamicSet.dynamicSet(classId, fieldId, object, value);
	}

	public static class DynamicNew {
		@JTranscAsync
		static public Object dynamicNew(int classId, int constructorId, Object[] params) {
			return null;
		}
	}

	public static class DynamicInvoke {
		@JTranscAsync
		static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
			Object ret = DynamicInvokeFirst.dynamicInvoke(classId, methodId, object, params);
			if (ret != null) {
				return ret;
			}
			ret = DynamicInvokeMiddle.dynamicInvoke(classId, methodId, object, params);
			if (ret != null) {
				return ret;
			}
			return DynamicInvokeLast.dynamicInvoke(classId, methodId, object, params);
		}
	}

	public static class DynamicInvokeFirst {
		@JTranscAsync
		static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
			return null;
		}
	}

	public static class DynamicInvokeMiddle {
		@JTranscAsync
		static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
			return null;
		}
	}

	public static class DynamicInvokeLast {
		@JTranscAsync
		static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
			return null;
		}
	}

	public static class DynamicGet {
		@JTranscSync
		static public Object dynamicGet(int classId, int fieldId, Object object) {
			return null;
		}
	}

	public static class DynamicSet {
		@JTranscSync
		static public void dynamicSet(int classId, int fieldId, Object object, Object value) {
		}
	}

	@JTranscSync
	static public Annotation[] getClassAnnotations(int classId) {
		return new Annotation[0];
	}

	@JTranscSync
	static public Annotation[] getFieldAnnotations(int classId, int fieldId) {
		return new Annotation[0];
	}

	@JTranscSync
	static public Annotation[] getMethodAnnotations(int classId, int methodId) {
		return new Annotation[0];
	}

	@JTranscSync
	static public Annotation[] getMethodArgumentAnnotations(int classId, int methodId, int argIndex) {
		return new Annotation[0];
	}

	@JTranscSync
	static public MemberInfo getMethodInfo(int classId, int methodId) {
		_ensureMethods();
		return _directMethodsInfo.get(classId).get(methodId);
	}

	//native static public Class<?> getClassByInfo(ClassInfo info);

	@JTranscSync
	native static public Method getMethodByInfo(Class<?> clazz, MemberInfo info);

	@JTranscSync
	static public Class<?> getClassById(int classId) {
		ProgramReflection._ensure();
		try {
			return Class.forName(_classNames[classId]);
		} catch (ClassNotFoundException e) {
			JTranscConsole.syncPrintStackTrace(e);
			return null;
		}
	}

	@JTranscSync
	static public Method getDirectMethod(int classId, int methodId) {
		return getMethodByInfo(getClassById(classId), getMethodInfo(classId, methodId));
	}

	//static public long dynamicFieldPtr(int fieldId, Object object) {
	//	return 0L;
	//}
}
