package javatest.misc;

import com.jtransc.io.JTranscConsole;

import java.util.ArrayList;
import java.util.Collections;

public class ArrayListTest {
	static public void main(String[] args) {
		arrayListTest();
	}

	static public void arrayListTest() {
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();

		JTranscConsole.log(list.size());
		sb.append(list.size());
		list.add("A");
		JTranscConsole.log("------------");
		JTranscConsole.log(list.size());
		sb.append(list.get(0));
		sb.append(list.size());
		list.add("B");
		list.add("C");
		sb.append(list.size());
		list.ensureCapacity(5);
		sb.append(list.size());
		list.trimToSize();
		sb.append(list.size());

		Collections.reverse(list);
		for (String item : list) sb.append(item);

		System.out.println("ArrayList:" + sb.toString());
	}

}
