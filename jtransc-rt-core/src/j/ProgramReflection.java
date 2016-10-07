package j;

import com.jtransc.annotation.JTranscMethodBody;

/**
 * MetaReflectionPlugin set those methods
 */
@SuppressWarnings("unused")
public class ProgramReflection {
	// Class
	static public ClassInfo[] getAllClasses() {
		return new ClassInfo[0];
	}

	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE({% CLASS java.lang.Object %}, p0)->__INSTANCE_CLASS_ID;")
	static public int getClassId(Object obj) {
		return -1;
	}

	static public MemberInfo[] getConstructors(int classId) {
		return new MemberInfo[0];
	}

	static public MemberInfo[] getMethods(int classId) {
		return new MemberInfo[0];
	}

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

	//static public long dynamicFieldPtr(int fieldId, Object object) {
	//	return 0L;
	//}
}
