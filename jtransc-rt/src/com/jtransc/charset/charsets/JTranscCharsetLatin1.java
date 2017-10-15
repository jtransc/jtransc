package com.jtransc.charset.charsets;

import com.jtransc.annotation.JTranscSync;
import com.jtransc.charset.JTranscCharsetSingleByte;

public class JTranscCharsetLatin1 extends JTranscCharsetSingleByte {
	@JTranscSync
	public JTranscCharsetLatin1() {
		super(
			new String[]{"ISO-8859-1", "819", "ISO8859-1", "L1", "ISO_8859-1:1987", "ISO_8859-1", "8859_1", "ISO-IR-100", "LATIN1", "CP819", "ISO8859_1", "IBM819", "ISO_8859_1", "IBM-819", "CSISOLATIN1"},
			"\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u007f\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089\u008a\u008b\u008c\u008d\u008e\u008f\u0090\u0091\u0092\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009a\u009b\u009c\u009d\u009e\u009f\u00a0¡¢£¤¥¦§¨©ª«¬­®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"
		);
	}
}
