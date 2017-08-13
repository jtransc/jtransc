package com.jtransc.text;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class JTranscLocaleTest {
	@Test
	public void name() throws Exception {
		Assert.assertEquals(".", JTranscLocale.getDecimalSeparator(Locale.ENGLISH));
		Assert.assertEquals(".", JTranscLocale.getDecimalSeparator(Locale.CHINESE));
		Assert.assertEquals(".", JTranscLocale.getDecimalSeparator(Locale.JAPAN));
		Assert.assertEquals(",", JTranscLocale.getDecimalSeparator(Locale.FRENCH));
	}
}