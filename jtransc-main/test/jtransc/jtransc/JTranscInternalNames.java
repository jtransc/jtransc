package jtransc.jtransc;

import com.jtransc.annotation.JTranscKeep;
import com.jtransc.reflection.JTranscReflection;

public class JTranscInternalNames {
	@JTranscKeep
	static public void main(String[] args) {
		System.out.println(JTranscReflection.getInternalName(JTranscInternalNames.class));
		System.out.println(JTranscReflection.getInternalName(JTranscInternalNames.class.getDeclaredMethods()[0]));
	}
}
