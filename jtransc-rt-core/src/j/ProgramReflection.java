package j;

import com.jtransc.JTranscSystem;
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
	static public FastIntMap<FastIntMap<MemberInfo>> _constructorsInfo;
	static public FastIntMap<FastIntMap<MemberInfo>> _methodInfos;
	static public FastIntMap<FastIntMap<MemberInfo>> _fieldsInfos;

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

	static public void _ensureConstructors() {
		if (_constructorsInfo != null) return;
		_ensure();

		_constructorsInfo = new FastIntMap<FastIntMap<MemberInfo>>();
		for (ClassInfo info : _classInfos) {
			if (info == null) continue;
			FastIntMap<MemberInfo> map = new FastIntMap<MemberInfo>();
			_constructorsInfo.set(info.id, map);
			MemberInfo[] minfo = getConstructors(info.id);
			if (minfo != null) for (MemberInfo i : minfo) map.set(i.id, i);
		}
	}

	static public void _ensureFields() {
		if (_fieldsInfos != null) return;
		_ensure();

		_fieldsInfos = new FastIntMap<FastIntMap<MemberInfo>>();
		for (ClassInfo info : _classInfos) {
			if (info == null) continue;
			FastIntMap<MemberInfo> map = new FastIntMap<MemberInfo>();
			_fieldsInfos.set(info.id, map);
			MemberInfo[] minfo = getFields(info.id);
			if (minfo != null) for (MemberInfo i : minfo) map.set(i.id, i);
		}
	}

	static public void _ensureMethods() {
		if (_methodInfos != null) return;
		_ensure();

		_methodInfos = new FastIntMap<FastIntMap<MemberInfo>>();
		for (ClassInfo info : _classInfos) {
			if (info == null) continue;
			FastIntMap<MemberInfo> map = new FastIntMap<MemberInfo>();
			_methodInfos.set(info.id, map);
			MemberInfo[] minfo = getMethods(info.id);
			if (minfo != null) for (MemberInfo i : minfo) map.set(i.id, i);
		}
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
		static public ClassInfo[] getAllClasses() {
			return new ClassInfo[0];
		}
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	static public MemberInfo[] getConstructors(int classId) {
		return AllConstructors.getConstructors(classId);
	}

	public static class AllConstructors {
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
			return new MemberInfo[0];
		}
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	static public MemberInfo[] getFields(int classId) {
		return AllFields.getFields(classId);
	}

	public static class AllFields {
		static public MemberInfo[] getFields(int classId) {
			return new MemberInfo[0];
		}
	}

	// Constructor
	static public Object dynamicNew(int classId, int constructorId, Object[] params) {
		return DynamicNewInvoke.dynamicNew(classId, constructorId, params);
	}

	// Method
	static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
		return DynamicNewInvoke.dynamicInvoke(classId, methodId, object, params);
	}

	// Field
	static public Object dynamicGet(int classId, int fieldId, Object object) {
		return DynamicGetSet.dynamicGet(classId, fieldId, object);
	}

	static public void dynamicSet(int classId, int fieldId, Object object, Object value) {
		DynamicGetSet.dynamicSet(classId, fieldId, object, value);
	}

	public static class DynamicNewInvoke {
		static public Object dynamicNew(int classId, int constructorId, Object[] params) {
			return null;
		}

		static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
			return null;
		}
	}

	public static class DynamicGetSet {
		static public Object dynamicGet(int classId, int fieldId, Object object) {
			return null;
		}

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
		return _methodInfos.get(classId).get(methodId);
	}

	//native static public Class<?> getClassByInfo(ClassInfo info);

	native static public Method getMethodByInfo(Class<?> clazz, MemberInfo info);

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
