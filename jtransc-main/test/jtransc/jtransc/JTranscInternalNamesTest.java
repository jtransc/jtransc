package jtransc.jtransc;

import com.jtransc.annotation.JTranscKeep;
import com.jtransc.reflection.JTranscInternalNames;

@SuppressWarnings("WeakerAccess")
public class JTranscInternalNamesTest {
	static public int _$hello = 10;

	@JTranscKeep
	static public void main(String[] args) {
		System.out.println(_$hello);
		System.out.println(JTranscInternalNames.getInternalClassName(JTranscInternalNamesTest.class));
		System.out.println(JTranscInternalNames.getInternalMethodName(JTranscInternalNamesTest.class.getDeclaredMethods()[0]));
		System.out.println(JTranscInternalNames.getInternalFieldName(JTranscInternalNamesTest.class.getDeclaredFields()[0]));
	}
}
