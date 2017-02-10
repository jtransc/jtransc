package java.lang.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.ds.FastStringMap;
import j.MemberInfo;
import j.ProgramReflection;
import j.ClassInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("ConstantConditions")
public class JTranscCoreReflection {
	static public <T> int[] getInterfaceIds(int classId) {
		ProgramReflection._ensure();
		return checkClassId(classId) ? ProgramReflection._classInfos[classId].interfaces : new int[0];
	}

	static private boolean checkClassId(int classId) {
		ProgramReflection._ensure();
		return classId >= 0 && classId < ProgramReflection._classInfos.length;
	}

	static public <T> int getSuperclassId(int classId) {
		ProgramReflection._ensure();
		return checkClassId(classId) ? ProgramReflection._classInfos[classId].parent : -1;
	}

	static public <T> Constructor<T>[] getDeclaredConstructors(Class<?> clazz) {
		MemberInfo[] infos = ProgramReflection.getConstructors(getClassId(clazz));
		int count = (infos != null) ? infos.length : 0;
		Constructor[] out = new Constructor[count];
		for (int n = 0; n < count; n++) {
			out[n] = new Constructor(clazz, infos[n]);
		}
		return out;
	}

	static public Method[] getDeclaredMethods(Class<?> clazz) {
		MemberInfo[] infos = ProgramReflection.getMethods(getClassId(clazz));
		int count = (infos != null) ? infos.length : 0;
		Method[] out = new Method[count];
		for (int n = 0; n < count; n++) {
			out[n] = new Method(clazz, infos[n]);
		}
		return out;
	}

	static public Field[] getDeclaredFields(Class<?> clazz) {
		MemberInfo[] infos = ProgramReflection.getFields(getClassId(clazz));
		int count = (infos != null) ? infos.length : 0;
		Field[] out = new Field[count];
		for (int n = 0; n < count; n++) {
			out[n] = new Field(clazz, infos[n]);
		}
		return out;
	}

	static public int getClassId(Class<?> clazz) {
		return clazz.id;
	}

	static private int getClassIdByName(String name) {
		ProgramReflection._ensure();
		return ProgramReflection._classInfosByName.get(name).id;
	}

	static public String getClassNameById(int id) {
		if (!checkClassId(id)) return null;
		ProgramReflection._ensure();
		return ProgramReflection._classInfos[id].name;
	}

	static public String[] getClassNames() {
		ProgramReflection._ensure();
		return ProgramReflection._classNames;
	}

	static public Class<?> getClassById(int id) {
		ProgramReflection._ensure();
		return getClassByName(getClassNameById(id));
	}

	static public Class<?> getClassByName(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	static public String getClassName(Class<?> clazz) {
		return clazz.getName();
	}

	static public boolean hasClassWithName(String name) {
		return ProgramReflection.hasClassWithName(name);
	}

	static public int getClassIdWithName(String name) {
		ProgramReflection._ensure();
		return hasClassWithName(name) ? ProgramReflection._classInfosByName.get(name).id : -1;
	}

	static public ClassInfo getClassInfoWithName(String name) {
		return ProgramReflection.getClassInfoWithName(name);
	}

	public static Annotation[] getDeclaredAnnotations(Class<?> clazz) {
		ProgramReflection._ensure();
		return ProgramReflection.getClassAnnotations(getClassId(clazz));
	}

	@JTranscInline
	public static int getModifiersWithId(int classId) {
		ProgramReflection._ensure();
		return ProgramReflection._classInfos[classId].modifiers;
	}

	@HaxeMethodBody("return (p0 != null) ? Std.is(p0, JA_0) : false;")
	@JTranscMethodBody(target = "js", value = "return p0 ? (p0 instanceof JA_0) : false;")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT(JA_0, p0) != null;")
	@JTranscMethodBody(target = "d", value = "return (cast(JA_0)p0) !is null;")
	@JTranscMethodBody(target = "cs", value = "return p0 is JA_0;")
	native public static boolean isArray(Object o);

	@HaxeMethodBody("return (p0 != null) ? N.str(cast(p0, JA_0).desc) : null;")
	@JTranscMethodBody(target = "js", value = "return p0 ? N.str(p0.desc) : null;")
	@JTranscMethodBody(target = "cpp", value = "return N::str(GET_OBJECT(JA_0, p0)->desc);")
	@JTranscMethodBody(target = "d", value = "return N.str((cast(JA_0)p0).desc);")
	@JTranscMethodBody(target = "cs", value = "return N.str(((JA_0)p0).desc);")
	native public static String getArrayDescriptor(Object o);

	@HaxeMethodBody(value = "return p0._CLASS_ID__HX;")
	@JTranscMethodBody(target = "js", value = "return p0.$$CLASS_ID;")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE({% CLASS java.lang.Object %}, p0)->__INSTANCE_CLASS_ID;")
	@JTranscMethodBody(target = "d", value = "return p0.__D__CLASS_ID;")
	@JTranscMethodBody(target = "cs", value = "return p0.__CS__CLASS_ID;")
	static public int getClassId(Object obj) {
		return -1;
	}
}
