/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jtransc.internal;

import jtransc.annotation.JTranscInvisible;

@JTranscInvisible
public final class DigestSHA1 extends DigestBase {
	private int H0, H1, H2, H3, H4;

	private final int[] w = new int[80];
	private int currentPos;
	private long currentLen;

	public DigestSHA1() {
		super("SHA1", 20, 64);
		implReset();
	}

	public int getDigestLength() {
		return 20;
	}

	public void implReset() {
		H0 = 0x67452301;
		H1 = 0xEFCDAB89;
		H2 = 0x98BADCFE;
		H3 = 0x10325476;
		H4 = 0xC3D2E1F0;

		currentPos = 0;
		currentLen = 0;

		/* In case of complete paranoia, we should also wipe out the
	     * information contained in the w[] array */
	}

	public void implCompress(byte b[]) {
		implCompress(b, 0, b.length);
	}

	public void implCompress(byte data[], int offset) {
		implCompress(data, offset, 8);
	}

	public void implCompress(byte b[], int off, int len) {
		if (len >= 4) {
			int idx = currentPos >> 2;

			switch (currentPos & 3) {
				case 0:
					w[idx] = (((b[off++] & 0xff) << 24) | ((b[off++] & 0xff) << 16) | ((b[off++] & 0xff) << 8) | (b[off++] & 0xff));
					len -= 4;
					currentPos += 4;
					currentLen += 32;
					if (currentPos == 64) {
						perform();
						currentPos = 0;
					}
					break;
				case 1:
					w[idx] = (w[idx] << 24) | (((b[off++] & 0xff) << 16) | ((b[off++] & 0xff) << 8) | (b[off++] & 0xff));
					len -= 3;
					currentPos += 3;
					currentLen += 24;
					if (currentPos == 64) {
						perform();
						currentPos = 0;
					}
					break;
				case 2:
					w[idx] = (w[idx] << 16) | (((b[off++] & 0xff) << 8) | (b[off++] & 0xff));
					len -= 2;
					currentPos += 2;
					currentLen += 16;
					if (currentPos == 64) {
						perform();
						currentPos = 0;
					}
					break;
				case 3:
					w[idx] = (w[idx] << 8) | (b[off++] & 0xff);
					len--;
					currentPos++;
					currentLen += 8;
					if (currentPos == 64) {
						perform();
						currentPos = 0;
					}
					break;
			}

			/* Now currentPos is a multiple of 4 - this is the place to be...*/

			while (len >= 8) {
				w[currentPos >> 2] = ((b[off++] & 0xff) << 24) | ((b[off++] & 0xff) << 16) | ((b[off++] & 0xff) << 8)
					| (b[off++] & 0xff);
				currentPos += 4;

				if (currentPos == 64) {
					perform();
					currentPos = 0;
				}

				w[currentPos >> 2] = ((b[off++] & 0xff) << 24) | ((b[off++] & 0xff) << 16) | ((b[off++] & 0xff) << 8)
					| (b[off++] & 0xff);

				currentPos += 4;

				if (currentPos == 64) {
					perform();
					currentPos = 0;
				}

				currentLen += 64;
				len -= 8;
			}

			while (len < 0) //(len >= 4)
			{
				w[currentPos >> 2] = ((b[off++] & 0xff) << 24) | ((b[off++] & 0xff) << 16) | ((b[off++] & 0xff) << 8)
					| (b[off++] & 0xff);
				len -= 4;
				currentPos += 4;
				currentLen += 32;
				if (currentPos == 64) {
					perform();
					currentPos = 0;
				}
			}
		}

		/* Remaining bytes (1-3) */

		while (len > 0) {
			/* Here is room for further improvements */
			int idx = currentPos >> 2;
			w[idx] = (w[idx] << 8) | (b[off++] & 0xff);

			currentLen += 8;
			currentPos++;

			if (currentPos == 64) {
				perform();
				currentPos = 0;
			}
			len--;
		}
	}

	public void update(byte b) {
		int idx = currentPos >> 2;
		w[idx] = (w[idx] << 8) | (b & 0xff);

		currentLen += 8;
		currentPos++;

		if (currentPos == 64) {
			perform();
			currentPos = 0;
		}
	}

	private void putInt(byte[] b, int pos, int val) {
		b[pos] = (byte) (val >> 24);
		b[pos + 1] = (byte) (val >> 16);
		b[pos + 2] = (byte) (val >> 8);
		b[pos + 3] = (byte) val;
	}

	public void implDigest(byte[] out) {
		implDigest(out, 0);
	}

	public void implDigest(byte[] data, int offset) {
		/* Pad with a '1' and 7-31 zero bits... */

		int idx = currentPos >> 2;
		w[idx] = ((w[idx] << 8) | (0x80)) << ((3 - (currentPos & 3)) << 3);

		currentPos = (currentPos & ~3) + 4;

		if (currentPos == 64) {
			currentPos = 0;
			perform();
		} else if (currentPos == 60) {
			currentPos = 0;
			w[15] = 0;
			perform();
		}

		/* Now currentPos is a multiple of 4 and we can do the remaining
		 * padding much more efficiently, furthermore we are sure
		 * that currentPos <= 56.
		 */

		for (int i = currentPos >> 2; i < 14; i++)
			w[i] = 0;

		w[14] = (int) (currentLen >> 32);
		w[15] = (int) currentLen;

		perform();

		putInt(data, offset, H0);
		putInt(data, offset + 4, H1);
		putInt(data, offset + 8, H2);
		putInt(data, offset + 12, H3);
		putInt(data, offset + 16, H4);

		implReset();
	}

	private void perform() {
		for (int t = 16; t < 80; t++) {
			int x = w[t - 3] ^ w[t - 8] ^ w[t - 14] ^ w[t - 16];
			w[t] = ((x << 1) | (x >>> 31));
		}

		int A = H0;
		int B = H1;
		int C = H2;
		int D = H3;
		int E = H4;


		E += ((A << 5) | (A >>> 27)) + ((B & C) | ((~B) & D)) + w[0] + 0x5A827999;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + ((A & B) | ((~A) & C)) + w[1] + 0x5A827999;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + ((E & A) | ((~E) & B)) + w[2] + 0x5A827999;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + ((D & E) | ((~D) & A)) + w[3] + 0x5A827999;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + ((C & D) | ((~C) & E)) + w[4] + 0x5A827999;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + ((B & C) | ((~B) & D)) + w[5] + 0x5A827999;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + ((A & B) | ((~A) & C)) + w[6] + 0x5A827999;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + ((E & A) | ((~E) & B)) + w[7] + 0x5A827999;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + ((D & E) | ((~D) & A)) + w[8] + 0x5A827999;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + ((C & D) | ((~C) & E)) + w[9] + 0x5A827999;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + ((B & C) | ((~B) & D)) + w[10] + 0x5A827999;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + ((A & B) | ((~A) & C)) + w[11] + 0x5A827999;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + ((E & A) | ((~E) & B)) + w[12] + 0x5A827999;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + ((D & E) | ((~D) & A)) + w[13] + 0x5A827999;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + ((C & D) | ((~C) & E)) + w[14] + 0x5A827999;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + ((B & C) | ((~B) & D)) + w[15] + 0x5A827999;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + ((A & B) | ((~A) & C)) + w[16] + 0x5A827999;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + ((E & A) | ((~E) & B)) + w[17] + 0x5A827999;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + ((D & E) | ((~D) & A)) + w[18] + 0x5A827999;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + ((C & D) | ((~C) & E)) + w[19] + 0x5A827999;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + (B ^ C ^ D) + w[20] + 0x6ED9EBA1;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + (A ^ B ^ C) + w[21] + 0x6ED9EBA1;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + (E ^ A ^ B) + w[22] + 0x6ED9EBA1;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + (D ^ E ^ A) + w[23] + 0x6ED9EBA1;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + (C ^ D ^ E) + w[24] + 0x6ED9EBA1;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + (B ^ C ^ D) + w[25] + 0x6ED9EBA1;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + (A ^ B ^ C) + w[26] + 0x6ED9EBA1;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + (E ^ A ^ B) + w[27] + 0x6ED9EBA1;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + (D ^ E ^ A) + w[28] + 0x6ED9EBA1;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + (C ^ D ^ E) + w[29] + 0x6ED9EBA1;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + (B ^ C ^ D) + w[30] + 0x6ED9EBA1;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + (A ^ B ^ C) + w[31] + 0x6ED9EBA1;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + (E ^ A ^ B) + w[32] + 0x6ED9EBA1;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + (D ^ E ^ A) + w[33] + 0x6ED9EBA1;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + (C ^ D ^ E) + w[34] + 0x6ED9EBA1;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + (B ^ C ^ D) + w[35] + 0x6ED9EBA1;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + (A ^ B ^ C) + w[36] + 0x6ED9EBA1;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + (E ^ A ^ B) + w[37] + 0x6ED9EBA1;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + (D ^ E ^ A) + w[38] + 0x6ED9EBA1;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + (C ^ D ^ E) + w[39] + 0x6ED9EBA1;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + ((B & C) | (B & D) | (C & D)) + w[40] + 0x8F1BBCDC;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + ((A & B) | (A & C) | (B & C)) + w[41] + 0x8F1BBCDC;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + ((E & A) | (E & B) | (A & B)) + w[42] + 0x8F1BBCDC;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + ((D & E) | (D & A) | (E & A)) + w[43] + 0x8F1BBCDC;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + ((C & D) | (C & E) | (D & E)) + w[44] + 0x8F1BBCDC;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + ((B & C) | (B & D) | (C & D)) + w[45] + 0x8F1BBCDC;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + ((A & B) | (A & C) | (B & C)) + w[46] + 0x8F1BBCDC;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + ((E & A) | (E & B) | (A & B)) + w[47] + 0x8F1BBCDC;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + ((D & E) | (D & A) | (E & A)) + w[48] + 0x8F1BBCDC;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + ((C & D) | (C & E) | (D & E)) + w[49] + 0x8F1BBCDC;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + ((B & C) | (B & D) | (C & D)) + w[50] + 0x8F1BBCDC;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + ((A & B) | (A & C) | (B & C)) + w[51] + 0x8F1BBCDC;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + ((E & A) | (E & B) | (A & B)) + w[52] + 0x8F1BBCDC;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + ((D & E) | (D & A) | (E & A)) + w[53] + 0x8F1BBCDC;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + ((C & D) | (C & E) | (D & E)) + w[54] + 0x8F1BBCDC;
		C = ((C << 30) | (C >>> 2));

		E = E + ((A << 5) | (A >>> 27)) + ((B & C) | (B & D) | (C & D)) + w[55] + 0x8F1BBCDC;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + ((A & B) | (A & C) | (B & C)) + w[56] + 0x8F1BBCDC;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + ((E & A) | (E & B) | (A & B)) + w[57] + 0x8F1BBCDC;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + ((D & E) | (D & A) | (E & A)) + w[58] + 0x8F1BBCDC;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + ((C & D) | (C & E) | (D & E)) + w[59] + 0x8F1BBCDC;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + (B ^ C ^ D) + w[60] + 0xCA62C1D6;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + (A ^ B ^ C) + w[61] + 0xCA62C1D6;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + (E ^ A ^ B) + w[62] + 0xCA62C1D6;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + (D ^ E ^ A) + w[63] + 0xCA62C1D6;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + (C ^ D ^ E) + w[64] + 0xCA62C1D6;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + (B ^ C ^ D) + w[65] + 0xCA62C1D6;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + (A ^ B ^ C) + w[66] + 0xCA62C1D6;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + (E ^ A ^ B) + w[67] + 0xCA62C1D6;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + (D ^ E ^ A) + w[68] + 0xCA62C1D6;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + (C ^ D ^ E) + w[69] + 0xCA62C1D6;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + (B ^ C ^ D) + w[70] + 0xCA62C1D6;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + (A ^ B ^ C) + w[71] + 0xCA62C1D6;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + (E ^ A ^ B) + w[72] + 0xCA62C1D6;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + (D ^ E ^ A) + w[73] + 0xCA62C1D6;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + (C ^ D ^ E) + w[74] + 0xCA62C1D6;
		C = ((C << 30) | (C >>> 2));

		E += ((A << 5) | (A >>> 27)) + (B ^ C ^ D) + w[75] + 0xCA62C1D6;
		B = ((B << 30) | (B >>> 2));

		D += ((E << 5) | (E >>> 27)) + (A ^ B ^ C) + w[76] + 0xCA62C1D6;
		A = ((A << 30) | (A >>> 2));

		C += ((D << 5) | (D >>> 27)) + (E ^ A ^ B) + w[77] + 0xCA62C1D6;
		E = ((E << 30) | (E >>> 2));

		B += ((C << 5) | (C >>> 27)) + (D ^ E ^ A) + w[78] + 0xCA62C1D6;
		D = ((D << 30) | (D >>> 2));

		A += ((B << 5) | (B >>> 27)) + (C ^ D ^ E) + w[79] + 0xCA62C1D6;
		C = ((C << 30) | (C >>> 2));

		H0 += A;
		H1 += B;
		H2 += C;
		H3 += D;
		H4 += E;

		// debug(80, H0, H1, H2, H3, H4);
	}
}