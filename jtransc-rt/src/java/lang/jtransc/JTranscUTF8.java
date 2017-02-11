package java.lang.jtransc;

import java.util.Arrays;

public class JTranscUTF8 {
	static public char[] decode(byte[] bytes, int offset, int length) {
		if (length <= 0) return new char[0];
		char[] out = new char[length];
		int o = 0;
		int i = offset;
		int end = offset + length;
		while (i < end) {
			int c = bytes[i++] & 0xFF;
			switch (c >> 4) {
				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7: {
					// 0xxxxxxx
					out[o++] = (char) (c);
					break;
				}
				case 12:
				case 13: {
					// 110x xxxx   10xx xxxx
					out[o++] = (char) (((c & 0x1F) << 6) | (bytes[i++] & 0x3F));
					break;
				}
				case 14: {
					// 1110 xxxx  10xx xxxx  10xx xxxx
					out[o++] = (char) (((c & 0x0F) << 12) | ((bytes[i++] & 0x3F) << 6) | ((bytes[i++] & 0x3F) << 0));
					break;
				}
			}
		}
		return (out.length == o) ? out : Arrays.copyOf(out, o);
	}

	/*
		var out, i, len, c;
    var char2, char3;

    out = "";
    len = array.length;
    i = 0;
    while(i < len) {
    c = array[i++];
    switch(c >> 4)
    {
      case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
        // 0xxxxxxx
        out += String.fromCharCode(c);
        break;
      case 12: case 13:
        // 110x xxxx   10xx xxxx
        char2 = array[i++];
        out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
        break;
      case 14:
        // 1110 xxxx  10xx xxxx  10xx xxxx
        char2 = array[i++];
        char3 = array[i++];
        out += String.fromCharCode(((c & 0x0F) << 12) |
                       ((char2 & 0x3F) << 6) |
                       ((char3 & 0x3F) << 0));
        break;
    }
    }

    return out;
	 */
}
