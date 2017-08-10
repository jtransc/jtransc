package jtransc;

import com.jtransc.annotation.JTranscAddFile;

@JTranscAddFile(target = "js", process = true, prepend = "extraref.js")
public class ExtraRefsTest {
	static public void main(String[] args) {
		System.out.println("OK");
	}
}
