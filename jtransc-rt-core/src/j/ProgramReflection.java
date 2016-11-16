package j;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.ds.FastStringMap;

import java.lang.annotation.Annotation;

/**
 * MetaReflectionPlugin set those methods
 */
@SuppressWarnings("unused")
public class ProgramReflection {
	static public ClassInfo[] _classInfos;
	static public String[] _classNames;
	static public FastStringMap<ClassInfo> _classInfosByName;

	static public void _ensure() {
		if (_classInfos != null) return;
		//System.out.println("ProgramReflection._ensure:");

		//System.out.println("[0]");
		_classInfosByName = new FastStringMap<>();
		//System.out.println("[1]");
		_classInfos = ProgramReflection.getAllClasses();
		//System.out.println("[2]");
		_classNames = new String[_classInfos.length];
		//System.out.println("[3]");
		//System.out.println(_classInfos);
		//System.out.println("ProgramReflection._ensure(len=" + _classInfos.length + "):");

		for (int n = 0; n < _classInfos.length; n++) {
			ClassInfo info = _classInfos[n];
			//System.out.println("POS:" + n);
			//System.out.println("" + n + " : " + (info != null));
			if (info != null) {
				//System.out.println("NAME:" + info.name);
				_classInfosByName.set(info.name, info);
				_classNames[n] = info.name;
			}
		}
		//System.out.println("[4]");
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
	static public Object dynamicNew(int constructorId, Object[] params) {
		return null;
	}

	// Method
	static public Object dynamicInvoke(int methodId, Object object, Object[] params) {
		return null;
	}

	// Field
	static public Object dynamicGet(int fieldId, Object object) {
		return null;
	}

	static public void dynamicSet(int fieldId, Object object, Object value) {
	}

	static public Annotation[] getClassAnnotations(int classId) {
		return new Annotation[0];
	}

	static public Annotation[] getFieldAnnotations(int fieldId) {
		return new Annotation[0];
	}

	static public Annotation[] getMethodAnnotations(int methodId) {
		return new Annotation[0];
	}

	static public Annotation[] getMethodArgumentAnnotations(int methodId, int argIndex) {
		return new Annotation[0];
	}

	//static public long dynamicFieldPtr(int fieldId, Object object) {
	//	return 0L;
	//}
}
