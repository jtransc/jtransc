package javatest.utils;

import java.util.HashMap;

public class MapTest {
	static public void main(String[] args) {
		testPutIfAbsent();
		testGetOrDefault();
	}

	static private void testPutIfAbsent() {
		System.out.println("testPutIfAbsent:");
		HashMap<Object, Object> map = new HashMap<>();
		map.put("a", "0");
		map.putIfAbsent("a", "1");
		map.putIfAbsent("b", "1");
		System.out.println(map.get("a"));
		System.out.println(map.get("b"));
	}

	static private void testGetOrDefault() {
		System.out.println("testGetOrDefault:");
		HashMap<Object, Object> map = new HashMap<>();
		map.put("a", "0");
		System.out.println(map.getOrDefault("a", "1"));
		System.out.println(map.getOrDefault("b", "1"));
	}
}
