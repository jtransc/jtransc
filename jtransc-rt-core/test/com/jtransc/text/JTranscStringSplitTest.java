package com.jtransc.text;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class JTranscStringSplitTest {
	@Test
	public void name() throws Exception {
		assertEquals(Arrays.asList(""), Arrays.asList(JTranscStringSplit.split("", '.')));
		assertEquals(Arrays.asList("test"), Arrays.asList(JTranscStringSplit.split("test", '.')));
		assertEquals(Arrays.asList("hello", "world"), Arrays.asList(JTranscStringSplit.split("hello.world", '.')));
		assertEquals(Arrays.asList("hello", "world", ""), Arrays.asList(JTranscStringSplit.split("hello.world.", '.')));
	}
}