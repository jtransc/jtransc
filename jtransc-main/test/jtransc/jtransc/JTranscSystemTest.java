package jtransc.jtransc;

import com.jtransc.JTranscSystem;

import java.util.Arrays;
import java.util.List;

public class JTranscSystemTest {
	static public void main(String[] args) {
		List<String> classes = Arrays.asList(JTranscSystem.getAllClasses());
		System.out.println(classes.size() >= 2);
		System.out.println(classes.contains(JTranscSystemTest.class.getName()));
		System.out.println(classes.contains("com.donot.exists"));
	}
}
