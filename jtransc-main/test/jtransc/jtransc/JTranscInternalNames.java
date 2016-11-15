package jtransc.jtransc;

import com.jtransc.annotation.JTranscKeep;
import com.jtransc.reflection.JTranscReflection;

@SuppressWarnings("WeakerAccess")
public class JTranscInternalNames {
	static public int _$hello = 10;

	@JTranscKeep
	static public void main(String[] args) {
		System.out.println(_$hello);
		System.out.println(JTranscReflection.getInternalName(JTranscInternalNames.class));
		System.out.println(JTranscReflection.getInternalName(JTranscInternalNames.class.getDeclaredMethods()[0]));
		System.out.println(JTranscReflection.getInternalName(JTranscInternalNames.class.getDeclaredFields()[0]));
	}
}
