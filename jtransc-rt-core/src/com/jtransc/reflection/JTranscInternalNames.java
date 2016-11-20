package com.jtransc.reflection;

import j.ClassInfo;
import j.MemberInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JTranscInternalNames {
	static private Field Class_info;
	static private Field Method_info;
	static private Field Field_info;

	static {
		Class_info = getFieldInAncestors(Class.class, "info");
		Method_info = getFieldInAncestors(Method.class, "info");
		Field_info = getFieldInAncestors(Field.class, "info");
	}

	private static Field getFieldInAncestors(Class<?> clazz, String name) {
		if (clazz == null) return null;
		try {
			Field field = clazz.getDeclaredField(name);
			if (field != null) return field;
		} catch (NoSuchFieldException e) {
		}
		return getFieldInAncestors(clazz.getSuperclass(), name);
	}

	static public String getInternalClassName(Class<?> clazz) {
		try {
			if (clazz != null && Class_info != null) {
				ClassInfo info = (ClassInfo) Class_info.get(clazz);
				if (info != null) return info.internalName;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return (clazz != null) ? clazz.getName() : "<UNKNOWN>";
	}

	static public String getInternalMethodName(Method method) {
		try {
			if (method != null && Method_info != null) {
				MemberInfo info = (MemberInfo) Method_info.get(method);
				if (info != null) return info.internalName;
				//return info.internalName;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return (method != null) ? method.getName() : "<UNKNOWN>";

	}

	static public String getInternalFieldName(Field field) {
		try {
			if (field != null && Field_info != null) {
				MemberInfo info = (MemberInfo) Field_info.get(field);
				if (info != null) return info.internalName;
				//return info.internalName;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return (field != null) ? field.getName() : "<UNKNOWN>";
	}

	static public String getInternalMemberName(MemberInfo info, String name) {
		return (info != null) ? info.internalName : null;
	}
}
