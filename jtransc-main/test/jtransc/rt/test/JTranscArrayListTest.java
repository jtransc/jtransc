package jtransc.rt.test;

import java.util.ArrayList;
import java.util.Arrays;

public class JTranscArrayListTest {
	static public void main(String[] args) {
		ArrayList<Integer> list = new ArrayList<>();
		list.add(1);
		System.out.println(list.size());
		list.add(2);
		list.add(3);
		list.add(99999);
		list.add(null);
		list.add(2);
		System.out.println(list.size());
		System.out.println(Arrays.toString(list.toArray()));
		System.out.println(list.indexOf(2));
		System.out.println(list.indexOf(null));
		System.out.println(list.lastIndexOf(2));
		System.out.println(list.indexOf(99999));
		System.out.println(list.indexOf(-7));
	}

}
