package com.jtransc.service;

import java.util.Iterator;
import java.util.ServiceLoader;

public class JTranscService {
	static public <T> T getFirst(Class<T> clazz) {
		Iterator<T> iterator = ServiceLoader.load(clazz).iterator();
		if (!iterator.hasNext()) throw new RuntimeException("Can't find service " + clazz);
		return iterator.next();
	}
}
