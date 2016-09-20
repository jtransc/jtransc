package big;

import com.jtransc.io.JTranscSyncIO;
import javatest.utils.Base64Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class HelloWorldTest {
	static public void main(String[] args) {
		ArrayList<Integer> items = new ArrayList<>();
		items.add(10);
		items.add(20);
		items.add(30);
		items.addAll(Arrays.asList(40, 50, 60));

		for (Integer item : items) {
			System.out.println(item);
		}

		HashMap<String, Object> map = new HashMap<>();
		map.put("hello", "world");
		System.out.println(map.size());


		Base64Test.main(args);
		/*
		//LinkedHashMap<String, Integer> stringIntegerLinkedHashMap = new LinkedHashMap<>();
		//stringIntegerLinkedHashMap.put("a", 10);
		//stringIntegerLinkedHashMap.put("b", 20);
		//stringIntegerLinkedHashMap.put("a", 11);
		//System.out.println("Hello World! : " + stringIntegerLinkedHashMap.get("a"));
		System.out.println("Hello World!");
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Hello World!");
		*/
		//System.out.println(new File("c:/temp/2.bin").length());
		//JTranscConsole.log("Hello World!");
	}
}
