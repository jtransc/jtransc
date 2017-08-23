package java.lang.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.ds.FastIntMap;
import j.MemberInfo;
import j.ProgramReflection;
import j.ClassInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("ConstantConditions")
public class JTranscCoreReflection {

	private static final FastIntMap<Constructor[]> _constructorsCache = new FastIntMap<Constructor[]>();
	private static final FastIntMap<Field[]> _fieldsCache = new FastIntMap<Field[]>();
	private static final FastIntMap<Method[]> _methodsCache = new FastIntMap<Method[]>();
	private static final FastIntMap<Annotation[]> _annotationsCache = new FastIntMap<Annotation[]>();

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

	static public Class<?> getSuperclassById(int classId) {
		return getClassById(getSuperclassId(classId));
	}

	static public <T> Constructor<T>[] getDeclaredConstructors(Class<?> clazz) {
		int classId = getClassId(clazz);
		Constructor[] cache = _constructorsCache.get(classId);
		if (cache != null) {
			return cache;
		}
		MemberInfo[] membersInfo = ProgramReflection.getConstructors(classId);
		int count = (membersInfo != null) ? membersInfo.length : 0;
		cache = new Constructor[count];
		for (int n = 0; n < count; n++) {
			cache[n] = new Constructor(clazz, membersInfo[n]);
		}
		_constructorsCache.set(classId, cache);
		return cache;
	}

	static public Method[] getDeclaredMethods(Class<?> clazz) {
		int classId = getClassId(clazz);
		Method[] cache = _methodsCache.get(classId);
		if (cache != null) {
			return cache;
		}
		MemberInfo[] membersInfo = ProgramReflection.getMethods(classId);
		int count = (membersInfo != null) ? membersInfo.length : 0;
		cache = new Method[count];
		for (int n = 0; n < count; n++) {
			cache[n] = new Method(clazz, membersInfo[n]);
		}
		_methodsCache.set(classId, cache);
		return cache;
	}

	static public Field[] getDeclaredFields(Class<?> clazz) {
		int classId = getClassId(clazz);
		Field[] cache = _fieldsCache.get(classId);
		if (cache != null) {
			return cache;
		}
		MemberInfo[] membersInfo = ProgramReflection.getFields(classId);
		int count = (membersInfo != null) ? membersInfo.length : 0;
		cache = new Field[count];
		for (int n = 0; n < count; n++) {
			cache[n] = new Field(clazz, membersInfo[n]);
		}
		_fieldsCache.set(classId, cache);
		return cache;
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
		int classId = getClassId(clazz);
		Annotation[] cache = _annotationsCache.get(classId);
		if (cache != null) {
			return cache;
		}
		cache = ProgramReflection.getClassAnnotations(getClassId(clazz));
		_annotationsCache.set(classId, cache);
		return cache;
	}

	@JTranscInline
	public static int getModifiersWithId(int classId) {
		ProgramReflection._ensure();
		return ProgramReflection._classInfos[classId].modifiers;
	}

	@HaxeMethodBody("return (p0 != null) ? Std.is(p0, JA_0) : false;")
	@JTranscMethodBody(target = "js", value = "return p0 ? (p0 instanceof JA_0) : false;")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT(JA_0, p0) != nullptr;")
	@JTranscMethodBody(target = "d", value = "return (cast(JA_0)p0) !is null;")
	@JTranscMethodBody(target = "cs", value = "return p0 is JA_0;")
	@JTranscMethodBody(target = "as3", value = "return p0 is JA_0;")
	@JTranscMethodBody(target = "dart", value = "return p0 is JA_0;")
	@JTranscMethodBody(target = "php", value = "return $p0 instanceof JA_0;")
	native public static boolean isArray(Object o);

	@HaxeMethodBody("return (p0 != null) ? N.str(cast(p0, JA_0).desc) : null;")
	@JTranscMethodBody(target = "js", value = "return p0 ? N.str(p0.desc) : null;")
	@JTranscMethodBody(target = "cpp", value = "return N::str(GET_OBJECT(JA_0, p0)->desc);")
	@JTranscMethodBody(target = "d", value = "return N.str((cast(JA_0)p0).desc);")
	@JTranscMethodBody(target = "cs", value = "return N.str(((JA_0)p0).desc);")
	@JTranscMethodBody(target = "as3", value = "return N.str((p0 as JA_0).desc);")
	@JTranscMethodBody(target = "dart", value = "return N.str((p0 as JA_0).desc);")
	@JTranscMethodBody(target = "php", value = "return N::str($p0->desc);")
	native public static String getArrayDescriptor(Object o);

	@HaxeMethodBody(value = "return p0.__JT__CLASS_ID;")
	@JTranscMethodBody(target = "js", value = "return p0.__JT__CLASS_ID;")
	@JTranscMethodBody(target = "cpp", value = "return GET_OBJECT_NPE({% CLASS java.lang.Object %}, p0)->__JT__CLASS_ID;")
	@JTranscMethodBody(target = "d", value = "return p0.__JT__CLASS_ID;")
	@JTranscMethodBody(target = "cs", value = "return p0.__JT__CLASS_ID;")
	@JTranscMethodBody(target = "as3", value = "return p0.__JT__CLASS_ID;")
	@JTranscMethodBody(target = "dart", value = "return p0.__JT__CLASS_ID;")
	@JTranscMethodBody(target = "php", value = "return $p0->__JT__CLASS_ID;")
	static public int getClassId(Object obj) {
		return -1;
	}
}
