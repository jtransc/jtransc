package java.lang.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.ds.FastStringMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JTranscCoreReflection {
	//static public <T> Field[] getDeclaredFields(Class<T> clazz) {
	//	return getDeclaredFields(getClassId(clazz));
	//}

	@JTranscMethodBody(target = "cpp", value = {
		"auto clazz = p0;",
		"int typeId = GET_OBJECT(java_lang_Class, clazz)->{% FIELD java.lang.Class:id %};",
		"auto table = TYPE_TABLE::TABLE[typeId];",
		"int len = table.constructorsSize;",
		"JA_L *array = new JA_L(len, L\"Ljava/lang/reflect/Constructor;\");",
		"SOBJ out = SOBJ(array);",
		"for (int n = 0; n < len; n++) {",
		"    auto c = new java_lang_reflect_Constructor();",
		"    auto info = table.constructors[n];",
		"    c->{% FIELD java.lang.reflect.Constructor:typeId %} = typeId;",
		"    c->{% FIELD java.lang.reflect.Constructor:slot %} = n;",
		"    c->{% FIELD java.lang.reflect.Constructor:name %} = N::str(L\"<init>\");",
		"    c->{% FIELD java.lang.reflect.Constructor:signature %} = N::str(info.desc);",
		"    c->{% FIELD java.lang.reflect.Constructor:genericSignature %} = N::str(info.genericDesc);",
		"    c->{% FIELD java.lang.reflect.Constructor:modifiers %} = info.flags;",
		"    c->{% FIELD java.lang.reflect.Constructor:clazz %} = clazz;",
		"    array->fastSet(n, SOBJ(c));",
		"}",
		"return out;",
	})
	native static public <T> Constructor<T>[] getDeclaredConstructors(Class<?> clazz);

	@JTranscMethodBody(target = "cpp", value = {
		"auto table = TYPE_TABLE::TABLE[p0];",
		"return SOBJ(JA_I::fromVector((int32_t *)table.interfaces, (int32_t)table.interfaceCount));",
	})
	native static public <T> int[] getInterfaceIds(int classId);

	@JTranscMethodBody(target = "cpp", value = "return TYPE_TABLE::TABLE[p0].superType;")
	native static public <T> int getSuperclassId(int classId);

	@JTranscMethodBody(target = "cpp", value = {
		"auto clazz = p0;",
		"int typeId = GET_OBJECT(java_lang_Class, clazz)->{% FIELD java.lang.Class:id %};",
		"auto table = TYPE_TABLE::TABLE[typeId];",
		"auto methods = table.methods;",
		"int len = table.methodsSize;",
		"JA_L *array = new JA_L(len, L\"Ljava/lang/reflect/Method;\");",
		"SOBJ out = SOBJ(array);",
		"for (int n = 0; n < len; n++) {",
		"    auto c = new java_lang_reflect_Method();",
		"    auto info = methods[n];",
		"    c->{% FIELD java.lang.reflect.Method:typeId %} = typeId;",
		"    c->{% FIELD java.lang.reflect.Method:slot %} = n;",
		"    c->{% FIELD java.lang.reflect.Method:name %} = N::str(info.name);",
		"    c->{% FIELD java.lang.reflect.Method:signature %} = N::str(info.desc);",
		"    c->{% FIELD java.lang.reflect.Method:genericSignature %} = N::str(info.genericDesc);",
		"    c->{% FIELD java.lang.reflect.Method:modifiers %} = info.flags;",
		"    c->{% FIELD java.lang.reflect.Method:clazz %} = clazz;",
		"    array->fastSet(n, SOBJ(c));",
		"}",
		"return out;",
	})
	native static public Method[] getDeclaredMethods(Class<?> clazz);

	@JTranscMethodBody(target = "cpp", value = {
		"auto clazz = p0;",
		"int typeId = GET_OBJECT(java_lang_Class, clazz)->{% FIELD java.lang.Class:id %};",
		"auto table = TYPE_TABLE::TABLE[typeId];",
		"auto fields = table.fields;",
		"int len = table.fieldsSize;",
		"JA_L *array = new JA_L(len, L\"Ljava/lang/reflect/Field;\");",
		"SOBJ out = SOBJ(array);",
		"for (int n = 0; n < len; n++) {",
		"    auto c = new java_lang_reflect_Field();",
		"    auto info = fields[n];",
		"    c->{% FIELD java.lang.reflect.Field:typeId %} = typeId;",
		"    c->{% FIELD java.lang.reflect.Field:slot %} = n;",
		"    c->{% FIELD java.lang.reflect.Field:name %} = N::str(info.name);",
		"    c->{% FIELD java.lang.reflect.Field:signature %} = N::str(info.desc);",
		"    c->{% FIELD java.lang.reflect.Field:genericSignature %} = N::str(info.genericDesc);",
		"    c->{% FIELD java.lang.reflect.Field:modifiers %} = info.flags;",
		"    c->{% FIELD java.lang.reflect.Field:clazz %} = clazz;",
		"    array->fastSet(n, SOBJ(c));",
		"}",
		"return out;",
	})
	native static public Field[] getDeclaredFields(Class<?> classId);

	static private String[] _classNames;
	static private FastStringMap<Integer> _classIds;

	@JTranscMethodBody(target = "cpp", value = "return TYPE_TABLE::count;")
	@JTranscInline
	native static private int _ensure_getClassCount();

	@JTranscMethodBody(target = "cpp", value = "return N::str(TYPE_TABLE::TABLE[p0].tname);")
	@JTranscInline
	native static private String _ensure_getClassNameById(int id);

	static private void _ensure() {
		if (_classNames == null) {
			_classIds = new FastStringMap<>();
			_classNames = new String[_ensure_getClassCount()];
			for (int id = 1; id < _classNames.length; id++) { // 0 is null!
				String name = _ensure_getClassNameById(id);
				//System.out.println("Class: " + name + " : " + id);
				_classNames[id] = name;
				_classIds.set(name, id);
			}
		}
	}

	static public int getClassId(Class<?> clazz) {
		//return getClassIdByName(clazz.getName());
		return clazz.id;
	}

	static private int getClassIdByName(String name) {
		_ensure();
		return _classIds.get(name);
	}

	static public String getClassNameById(int id) {
		if (id < 0) return null;
		_ensure();
		return _classNames[id];
	}

	static public String[] getClassNames() {
		_ensure();
		return _classNames;
	}

	static public String getClassName(Class<?> clazz) {
		return clazz.getName();
	}
	//static public int getClassId(Class<?> clazz) {
		//return clazz.id;
	//}

	static public boolean hasClassWithName(String name) {
		_ensure();
		return _classIds.has(name);
	}

	static public int getClassIdWithName(String name) {
		_ensure();
		return hasClassWithName(name) ? _classIds.get(name) : -1;
	}

	public static Annotation[] getDeclaredAnnotations() {
		return new Annotation[0];
	}

	@JTranscMethodBody(target = "cpp", value = "return TYPE_TABLE::TABLE[p0].flags;")
	@JTranscInline
	native public static int getModifiersWithId(int classId);
}
