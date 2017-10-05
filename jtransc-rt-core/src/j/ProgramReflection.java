package j;

import com.jtransc.JTranscSystem;
import com.jtransc.ds.FastIntMap;
import com.jtransc.ds.FastStringMap;

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

	@SuppressWarnings("ConstantConditions")
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

	private static boolean _ensureMethodsDone = false;
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

	static public boolean hasClassWithName(String name) {
		ProgramReflection._ensure();
		return ProgramReflection._classInfosByName.has(name);
	}

	static public ClassInfo getClassInfoWithName(String name) {
		ProgramReflection._ensure();
		return hasClassWithName(name) ? ProgramReflection._classInfosByName.get(name) : null;
	}

	// Class
	static public ClassInfo[] getAllClasses() {
		return _classInfos != null ? _classInfos : AllClasses.getAllClasses();
	}

	public static class AllClasses {
		public ClassInfo[] test;
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

		static public int getAllClassesCount() {
			return 0;
		}

		static public ClassInfo[] getAllClasses3000() {
			return new ClassInfo[0];
		}

		static public ClassInfo[] getAllClasses6000() {
			return new ClassInfo[0];
		}

		static public ClassInfo[] getAllClasses9000() {
			return new ClassInfo[0];
		}

		static public ClassInfo[] getAllClasses12000() {
			return new ClassInfo[0];
		}

		static public ClassInfo[] getAllClasses15000() {
			return new ClassInfo[0];
		}

		static public ClassInfo[] getAllClassesMax() {
			return new ClassInfo[0];
		}
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	static public MemberInfo[] getConstructors(int classId) {
		return AllConstructors.getConstructors(classId);
	}

	public static class AllConstructors {
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
		static public MemberInfo[] getConstructors(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllConstructorsMiddle {
		static public MemberInfo[] getConstructors(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllConstructorsLast {
		static public MemberInfo[] getConstructors(int classId) {
			return new MemberInfo[0];
		}
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	static public MemberInfo[] getMethods(int classId) {
		return AllMethods.getMethods(classId);
	}

	public static class AllMethods {
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
		static public MemberInfo[] getMethods(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllMethodsMiddle {
		static public MemberInfo[] getMethods(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllMethodsLast {
		static public MemberInfo[] getMethods(int classId) {
			return new MemberInfo[0];
		}
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	static public MemberInfo[] getFields(int classId) {
		return AllFields.getFields(classId);
	}

	public static class AllFields {
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
		static public MemberInfo[] getFields(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllFieldsMiddle {
		static public MemberInfo[] getFields(int classId) {
			return new MemberInfo[0];
		}
	}

	public static class AllFieldsLast {
		static public MemberInfo[] getFields(int classId) {
			return new MemberInfo[0];
		}
	}

	// Constructor
	static public Object dynamicNew(int classId, int constructorId, Object[] params) {
		return DynamicNew.dynamicNew(classId, constructorId, params);
	}

	// Method
	static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
		return DynamicInvoke.dynamicInvoke(classId, methodId, object, params);
	}

	// Field
	static public Object dynamicGet(int classId, int fieldId, Object object) {
		return DynamicGet.dynamicGet(classId, fieldId, object);
	}

	static public void dynamicSet(int classId, int fieldId, Object object, Object value) {
		DynamicSet.dynamicSet(classId, fieldId, object, value);
	}

	public static class DynamicNew {
		static public Object dynamicNew(int classId, int constructorId, Object[] params) {
			return null;
		}
	}

	public static class DynamicInvoke {
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
		static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
			return null;
		}
	}

	public static class DynamicInvokeMiddle {
		static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
			return null;
		}
	}

	public static class DynamicInvokeLast {
		static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
			return null;
		}
	}

	public static class DynamicGet {
		static public Object dynamicGet(int classId, int fieldId, Object object) {
			return null;
		}
	}

	public static class DynamicSet {
		static public void dynamicSet(int classId, int fieldId, Object object, Object value) {
		}
	}

	static public Annotation[] getClassAnnotations(int classId) {
		return new Annotation[0];
	}

	static public Annotation[] getFieldAnnotations(int classId, int fieldId) {
		return new Annotation[0];
	}

	static public Annotation[] getMethodAnnotations(int classId, int methodId) {
		return new Annotation[0];
	}

	static public Annotation[] getMethodArgumentAnnotations(int classId, int methodId, int argIndex) {
		return new Annotation[0];
	}

	static public MemberInfo getMethodInfo(int classId, int methodId) {
		_ensureMethods();
		return _directMethodsInfo.get(classId).get(methodId);
	}

	//native static public Class<?> getClassByInfo(ClassInfo info);

	static public Method getMethodByInfo(Class<?> clazz, MemberInfo info){
		JTranscSystem.checkInJVM("ProgramReflection::getMethodByInfo should've been replaced by plugin!");
		return null;
	}

	static public Class<?> getClassById(int classId) {
		ProgramReflection._ensure();
		try {
			return Class.forName(_classNames[classId]);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	static public Method getDirectMethod(int classId, int methodId) {
		return getMethodByInfo(getClassById(classId), getMethodInfo(classId, methodId));
	}

	//static public long dynamicFieldPtr(int fieldId, Object object) {
	//	return 0L;
	//}
}
