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

import jtransc.JTranscBits;
import jtransc.annotation.JTranscInvisible;

@JTranscInvisible
public final class DigestMD5 extends DigestBase {
	private int[] state = new int[4];
	private int[] x = new int[16];

	public DigestMD5() {
		super("MD5", 16, 64);
		this.implReset();
	}

	public Object clone() throws CloneNotSupportedException {
		DigestMD5 var1 = (DigestMD5) super.clone();
		var1.state = (int[]) var1.state.clone();
		var1.x = new int[16];
		return var1;
	}

	void implReset() {
		this.state[0] = 1732584193;
		this.state[1] = -271733879;
		this.state[2] = -1732584194;
		this.state[3] = 271733878;
	}

	void implDigest(byte[] data, int offset) {
		long var3 = this.processedLength << 3;
		int var5 = (int) this.processedLength & 63;
		int var6 = var5 < 56 ? 56 - var5 : 120 - var5;
		this.engineUpdate(padding, 0, var6);
		JTranscBits.i2bLittle4((int) (var3 >>> 0), this.buffer, 56);
		JTranscBits.i2bLittle4((int) (var3 >>> 32), this.buffer, 60);
		this.implCompress(this.buffer, 0);
		JTranscBits.i2bLittle(this.state, 0, data, offset, 16);
	}

	private static int FF(int a, int b, int c, int d, int x, int s, int t) {
		a += (b & c | ~b & d) + x + t;
		return (a << s | a >>> 32 - s) + b;
	}

	private static int GG(int a, int b, int c, int d, int x, int s, int t) {
		a += (b & d | c & ~d) + x + t;
		return (a << s | a >>> 32 - s) + b;
	}

	private static int HH(int a, int b, int c, int d, int x, int s, int t) {
		a += (b ^ c ^ d) + x + t;
		return (a << s | a >>> 32 - s) + b;
	}

	private static int II(int a, int b, int c, int d, int x, int s, int t) {
		a += (c ^ (b | ~d)) + x + t;
		return (a << s | a >>> 32 - s) + b;
	}

	void implCompress(byte[] data, int offset) {
		JTranscBits.b2iLittle64(data, offset, this.x);
		int a = this.state[0];
		int b = this.state[1];
		int c = this.state[2];
		int d = this.state[3];
		a = FF(a, b, c, d, this.x[0], 7, -680876936);
		d = FF(d, a, b, c, this.x[1], 12, -389564586);
		c = FF(c, d, a, b, this.x[2], 17, 606105819);
		b = FF(b, c, d, a, this.x[3], 22, -1044525330);
		a = FF(a, b, c, d, this.x[4], 7, -176418897);
		d = FF(d, a, b, c, this.x[5], 12, 1200080426);
		c = FF(c, d, a, b, this.x[6], 17, -1473231341);
		b = FF(b, c, d, a, this.x[7], 22, -45705983);
		a = FF(a, b, c, d, this.x[8], 7, 1770035416);
		d = FF(d, a, b, c, this.x[9], 12, -1958414417);
		c = FF(c, d, a, b, this.x[10], 17, -42063);
		b = FF(b, c, d, a, this.x[11], 22, -1990404162);
		a = FF(a, b, c, d, this.x[12], 7, 1804603682);
		d = FF(d, a, b, c, this.x[13], 12, -40341101);
		c = FF(c, d, a, b, this.x[14], 17, -1502002290);
		b = FF(b, c, d, a, this.x[15], 22, 1236535329);
		a = GG(a, b, c, d, this.x[1], 5, -165796510);
		d = GG(d, a, b, c, this.x[6], 9, -1069501632);
		c = GG(c, d, a, b, this.x[11], 14, 643717713);
		b = GG(b, c, d, a, this.x[0], 20, -373897302);
		a = GG(a, b, c, d, this.x[5], 5, -701558691);
		d = GG(d, a, b, c, this.x[10], 9, 38016083);
		c = GG(c, d, a, b, this.x[15], 14, -660478335);
		b = GG(b, c, d, a, this.x[4], 20, -405537848);
		a = GG(a, b, c, d, this.x[9], 5, 568446438);
		d = GG(d, a, b, c, this.x[14], 9, -1019803690);
		c = GG(c, d, a, b, this.x[3], 14, -187363961);
		b = GG(b, c, d, a, this.x[8], 20, 1163531501);
		a = GG(a, b, c, d, this.x[13], 5, -1444681467);
		d = GG(d, a, b, c, this.x[2], 9, -51403784);
		c = GG(c, d, a, b, this.x[7], 14, 1735328473);
		b = GG(b, c, d, a, this.x[12], 20, -1926607734);
		a = HH(a, b, c, d, this.x[5], 4, -378558);
		d = HH(d, a, b, c, this.x[8], 11, -2022574463);
		c = HH(c, d, a, b, this.x[11], 16, 1839030562);
		b = HH(b, c, d, a, this.x[14], 23, -35309556);
		a = HH(a, b, c, d, this.x[1], 4, -1530992060);
		d = HH(d, a, b, c, this.x[4], 11, 1272893353);
		c = HH(c, d, a, b, this.x[7], 16, -155497632);
		b = HH(b, c, d, a, this.x[10], 23, -1094730640);
		a = HH(a, b, c, d, this.x[13], 4, 681279174);
		d = HH(d, a, b, c, this.x[0], 11, -358537222);
		c = HH(c, d, a, b, this.x[3], 16, -722521979);
		b = HH(b, c, d, a, this.x[6], 23, 76029189);
		a = HH(a, b, c, d, this.x[9], 4, -640364487);
		d = HH(d, a, b, c, this.x[12], 11, -421815835);
		c = HH(c, d, a, b, this.x[15], 16, 530742520);
		b = HH(b, c, d, a, this.x[2], 23, -995338651);
		a = II(a, b, c, d, this.x[0], 6, -198630844);
		d = II(d, a, b, c, this.x[7], 10, 1126891415);
		c = II(c, d, a, b, this.x[14], 15, -1416354905);
		b = II(b, c, d, a, this.x[5], 21, -57434055);
		a = II(a, b, c, d, this.x[12], 6, 1700485571);
		d = II(d, a, b, c, this.x[3], 10, -1894986606);
		c = II(c, d, a, b, this.x[10], 15, -1051523);
		b = II(b, c, d, a, this.x[1], 21, -2054922799);
		a = II(a, b, c, d, this.x[8], 6, 1873313359);
		d = II(d, a, b, c, this.x[15], 10, -30611744);
		c = II(c, d, a, b, this.x[6], 15, -1560198380);
		b = II(b, c, d, a, this.x[13], 21, 1309151649);
		a = II(a, b, c, d, this.x[4], 6, -145523070);
		d = II(d, a, b, c, this.x[11], 10, -1120210379);
		c = II(c, d, a, b, this.x[2], 15, 718787259);
		b = II(b, c, d, a, this.x[9], 21, -343485551);
		this.state[0] += a;
		this.state[1] += b;
		this.state[2] += c;
		this.state[3] += d;
	}
}
