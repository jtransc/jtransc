package javatest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class JacocoFilters {
	static public Field[] filter(Field[] fields) {
		ArrayList<Field> out = new ArrayList<>();
		for (Field it : Arrays.asList(fields)) {
			if (!it.getName().startsWith("$jacoco")) out.add(it);
		}
		return out.toArray(new Field[0]);
	}

	static public Method[] filter(Method[] fields) {
		ArrayList<Method> out = new ArrayList<>();
		for (Method it : Arrays.asList(fields)) {
			if (!it.getName().startsWith("$jacoco")) out.add(it);
		}
		return out.toArray(new Method[0]);
	}

	static public Constructor[] filter(Constructor[] fields) {
		ArrayList<Constructor> out = new ArrayList<>();
		for (Constructor it : Arrays.asList(fields)) {
			if (!it.getName().startsWith("$jacoco")) out.add(it);
		}
		return out.toArray(new Constructor[0]);
	}
}
