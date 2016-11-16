package java.lang.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;
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
	//static public <T> Field[] getDeclaredFields(Class<T> clazz) {
	//	return getDeclaredFields(getClassId(clazz));
	//}

	//@JTranscMethodBody(target = "cpp", value = {
	//	"auto table = TYPE_TABLE::TABLE[p0];",
	//	"return SOBJ(JA_I::fromVector((int32_t *)table.interfaces, (int32_t)table.interfaceCount));",
	//})
	static public <T> int[] getInterfaceIds(int classId) {
		ProgramReflection._ensure();
		return checkClassId(classId) ? ProgramReflection._classInfos[classId].interfaces : new int[0];
	}

	static private boolean checkClassId(int classId) {
		ProgramReflection._ensure();
		return classId >= 0 && classId < ProgramReflection._classInfos.length;
	}

	//@JTranscMethodBody(target = "cpp", value = "return TYPE_TABLE::TABLE[p0].superType;")
	static public <T> int getSuperclassId(int classId) {
		ProgramReflection._ensure();
		return checkClassId(classId) ? ProgramReflection._classInfos[classId].parent : 0;
	}


	//@JTranscMethodBody(target = "cpp", value = {
	//	"auto clazz = p0;",
	//	"int typeId = GET_OBJECT(java_lang_Class, clazz)->{% FIELD java.lang.Class:id %};",
	//	"auto table = TYPE_TABLE::TABLE[typeId];",
	//	"int len = table.constructorsSize;",
	//	"JA_L *array = new JA_L(len, L\"Ljava/lang/reflect/Constructor;\");",
	//	"SOBJ out = SOBJ(array);",
	//	"for (int n = 0; n < len; n++) {",
	//	"    auto c = new java_lang_reflect_Constructor();",
	//	"    auto info = table.constructors[n];",
	//	"    c->{% FIELD java.lang.reflect.Constructor:typeId %} = typeId;",
	//	"    c->{% FIELD java.lang.reflect.Constructor:slot %} = n;",
	//	"    c->{% FIELD java.lang.reflect.Constructor:name %} = N::str(L\"<init>\");",
	//	"    c->{% FIELD java.lang.reflect.Constructor:signature %} = N::str(info.desc);",
	//	"    c->{% FIELD java.lang.reflect.Constructor:genericSignature %} = N::str(info.genericDesc);",
	//	"    c->{% FIELD java.lang.reflect.Constructor:modifiers %} = info.flags;",
	//	"    c->{% FIELD java.lang.reflect.Constructor:clazz %} = clazz;",
	//	"    array->fastSet(n, SOBJ(c));",
	//	"}",
	//	"return out;",
	//})
	static public <T> Constructor<T>[] getDeclaredConstructors(Class<?> clazz) {
		MemberInfo[] infos = ProgramReflection.getConstructors(getClassId(clazz));
		int count = (infos != null) ? infos.length : 0;
		Constructor[] out = new Constructor[count];
		for (int n = 0; n < count; n++) {
			out[n] = new Constructor(clazz, infos[n]);
		}
		return out;
	}

	//@JTranscMethodBody(target = "cpp", value = {
	//	"auto clazz = p0;",
	//	"int typeId = GET_OBJECT(java_lang_Class, clazz)->{% FIELD java.lang.Class:id %};",
	//	"auto table = TYPE_TABLE::TABLE[typeId];",
	//	"auto methods = table.methods;",
	//	"int len = table.methodsSize;",
	//	"JA_L *array = new JA_L(len, L\"Ljava/lang/reflect/Method;\");",
	//	"SOBJ out = SOBJ(array);",
	//	"for (int n = 0; n < len; n++) {",
	//	"    auto c = new java_lang_reflect_Method();",
	//	"    auto info = methods[n];",
	//	"    c->{% FIELD java.lang.reflect.Method:typeId %} = typeId;",
	//	"    c->{% FIELD java.lang.reflect.Method:slot %} = n;",
	//	"    c->{% FIELD java.lang.reflect.Method:name %} = N::str(info.name);",
	//	"    c->{% FIELD java.lang.reflect.Method:signature %} = N::str(info.desc);",
	//	"    c->{% FIELD java.lang.reflect.Method:genericSignature %} = N::str(info.genericDesc);",
	//	"    c->{% FIELD java.lang.reflect.Method:modifiers %} = info.flags;",
	//	"    c->{% FIELD java.lang.reflect.Method:clazz %} = clazz;",
	//	"    array->fastSet(n, SOBJ(c));",
	//	"}",
	//	"return out;",
	//})
	static public Method[] getDeclaredMethods(Class<?> clazz) {
		MemberInfo[] infos = ProgramReflection.getMethods(getClassId(clazz));
		int count = (infos != null) ? infos.length : 0;
		Method[] out = new Method[count];
		for (int n = 0; n < count; n++) {
			out[n] = new Method(clazz, infos[n]);
		}
		return out;
	}

	//@JTranscMethodBody(target = "cpp", value = {
	//	"auto clazz = p0;",
	//	"int typeId = GET_OBJECT(java_lang_Class, clazz)->{% FIELD java.lang.Class:id %};",
	//	"auto table = TYPE_TABLE::TABLE[typeId];",
	//	"auto fields = table.fields;",
	//	"int len = table.fieldsSize;",
	//	"JA_L *array = new JA_L(len, L\"Ljava/lang/reflect/Field;\");",
	//	"SOBJ out = SOBJ(array);",
	//	"for (int n = 0; n < len; n++) {",
	//	"    auto c = new java_lang_reflect_Field();",
	//	"    auto info = fields[n];",
	//	"    c->{% FIELD java.lang.reflect.Field:typeId %} = typeId;",
	//	"    c->{% FIELD java.lang.reflect.Field:slot %} = n;",
	//	"    c->{% FIELD java.lang.reflect.Field:name %} = N::str(info.name);",
	//	"    c->{% FIELD java.lang.reflect.Field:signature %} = N::str(info.desc);",
	//	"    c->{% FIELD java.lang.reflect.Field:genericSignature %} = N::str(info.genericDesc);",
	//	"    c->{% FIELD java.lang.reflect.Field:modifiers %} = info.flags;",
	//	"    c->{% FIELD java.lang.reflect.Field:clazz %} = clazz;",
	//	"    array->fastSet(n, SOBJ(c));",
	//	"}",
	//	"return out;",
	//})
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
		//return getClassIdByName(clazz.getName());
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
	//static public int getClassId(Class<?> clazz) {
	//return clazz.id;
	//}

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
}
