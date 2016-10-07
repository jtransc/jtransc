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
		return _classInfos[classId].interfaces;
	}

	//@JTranscMethodBody(target = "cpp", value = "return TYPE_TABLE::TABLE[p0].superType;")
	static public <T> int getSuperclassId(int classId) {
		_ensure();
		return _classInfos[classId].parent;
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

	static private ClassInfo[] _classInfos;
	static private String[] _classNames;
	static private FastStringMap<ClassInfo> _classInfosByName;

	static private void _ensure() {
		if (_classInfos != null) return;
		_classInfosByName = new FastStringMap<>();
		_classInfos = ProgramReflection.getAllClasses();
		_classNames = new String[_classInfos.length];
		for (int n = 1; n < _classInfos.length; n++) {
			ClassInfo info = _classInfos[n];
			_classInfosByName.set(info.name, info);
			_classNames[n] = info.name;
		}
	}

	static public int getClassId(Class<?> clazz) {
		//return getClassIdByName(clazz.getName());
		return clazz.id;
	}

	static private int getClassIdByName(String name) {
		_ensure();
		return _classInfosByName.get(name).id;
	}

	static public String getClassNameById(int id) {
		if (id < 0) return null;
		_ensure();
		return _classInfos[id].name;
	}

	static public String[] getClassNames() {
		_ensure();
		return _classNames;
	}

	static public Class<?> getClassById(int id) {
		_ensure();
		return Class.forName0(getClassNameById(id));
	}

	static public String getClassName(Class<?> clazz) {
		return clazz.getName();
	}
	//static public int getClassId(Class<?> clazz) {
	//return clazz.id;
	//}

	static public boolean hasClassWithName(String name) {
		_ensure();
		return _classInfosByName.has(name);
	}

	static public int getClassIdWithName(String name) {
		_ensure();
		return hasClassWithName(name) ? _classInfosByName.get(name).id : -1;
	}

	static public ClassInfo getClassInfoWithName(String name) {
		_ensure();
		return hasClassWithName(name) ? _classInfosByName.get(name) : null;
	}

	public static Annotation[] getDeclaredAnnotations() {
		return new Annotation[0];
	}

	@JTranscInline
	public static int getModifiersWithId(int classId) {
		_ensure();
		return _classInfos[classId].modifiers;
	}
}
