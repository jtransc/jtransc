package jtransc.jtransc.js;

import com.jtransc.annotation.JTranscRunCommand;

@JTranscRunCommand(target = "js", value = { "node", "{{ outputFile }}", "hello", "world" })
public class CustomJsRunTest {
	static public void main(String[] args) {
		System.out.println(args.length);
		for (String arg : args) {
			System.out.println(arg);
		}
	}
}
