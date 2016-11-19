package j;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.ds.FastIntMap;
import com.jtransc.ds.FastStringMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * MetaReflectionPlugin set those methods
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ProgramReflection {
	static public ClassInfo[] _classInfos;
	static public String[] _classNames;
	static public FastStringMap<ClassInfo> _classInfosByName;
	static public FastIntMap<FastIntMap<MemberInfo>> _constructorsInfo;
	static public FastIntMap<FastIntMap<MemberInfo>> _methodInfos;
	static public FastIntMap<FastIntMap<MemberInfo>> _fieldsInfos;

	@SuppressWarnings("ConstantConditions")
	static public void _ensure() {
		if (_classInfos != null) return;

		_classInfosByName = new FastStringMap<>();
		_classInfos = ProgramReflection.getAllClasses();
		_classNames = new String[_classInfos.length];

		_constructorsInfo = new FastIntMap<>();
		_methodInfos = new FastIntMap<>();
		_fieldsInfos = new FastIntMap<>();

		for (ClassInfo info : _classInfos) {
			if (info == null) continue;

			FastIntMap<MemberInfo> ci = new FastIntMap<>();
			FastIntMap<MemberInfo> mi = new FastIntMap<>();
			FastIntMap<MemberInfo> fi = new FastIntMap<>();

			_classInfosByName.set(info.name, info);
			_classNames[info.id] = info.name;

			_constructorsInfo.set(info.id, ci);
			_methodInfos.set(info.id, mi);
			_fieldsInfos.set(info.id, fi);

			MemberInfo[] constructors = getConstructors(info.id);
			MemberInfo[] methods = getMethods(info.id);
			MemberInfo[] fields = getFields(info.id);
			if (constructors != null) for (MemberInfo i : constructors) ci.set(i.id, i);
			if (methods != null) for (MemberInfo i : methods) mi.set(i.id, i);
			if (fields != null) for (MemberInfo i : fields) fi.set(i.id, i);

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
		return new ClassInfo[0];
	}

	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE({% CLASS java.lang.Object %}, p0)->__INSTANCE_CLASS_ID;")
	@JTranscMethodBody(target = "js", value = "return p0['_JS_$$CLASS_ID'];")
	static public int getClassId(Object obj) {
		return -1;
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	static public MemberInfo[] getConstructors(int classId) {
		return new MemberInfo[0];
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	static public MemberInfo[] getMethods(int classId) {
		return new MemberInfo[0];
	}

	// @NOTE: This will be replaced by MetaReflectionPlugin
	static public MemberInfo[] getFields(int classId) {
		return new MemberInfo[0];
	}

	// Constructor
	static public Object dynamicNew(int classId, int constructorId, Object[] params) {
		return null;
	}

	// Method
	static public Object dynamicInvoke(int classId, int methodId, Object object, Object[] params) {
		return null;
	}

	// Field
	static public Object dynamicGet(int classId, int fieldId, Object object) {
		return null;
	}

	static public void dynamicSet(int classId, int fieldId, Object object, Object value) {
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
		_ensure();
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
