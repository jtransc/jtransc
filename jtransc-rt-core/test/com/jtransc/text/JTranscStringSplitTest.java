package com.jtransc.text;

import com.jtransc.util.JTranscStrings;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class JTranscStringSplitTest {
	@Test
	public void name() throws Exception {
		assertEquals(Arrays.asList(""), Arrays.asList(JTranscStrings.split("", '.')));
		assertEquals(Arrays.asList("test"), Arrays.asList(JTranscStrings.split("test", '.')));
		assertEquals(Arrays.asList("hello", "world"), Arrays.asList(JTranscStrings.split("hello.world", '.')));
		assertEquals(Arrays.asList("hello", "world", ""), Arrays.asList(JTranscStrings.split("hello.world.", '.')));
	}
}