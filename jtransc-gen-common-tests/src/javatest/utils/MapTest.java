package javatest.utils;

import java.util.HashMap;

public class MapTest {
	static public void main(String[] args) {
		testPutIfAbsent();
	}

	static private void testPutIfAbsent() {
		HashMap<Object, Object> map = new HashMap<>();
		map.put("a", "0");
		map.putIfAbsent("a", "1");
		map.putIfAbsent("b", "1");
		System.out.println("testPutIfAbsent:");
		System.out.println(map.get("a"));
		System.out.println(map.get("b"));
	}
}
