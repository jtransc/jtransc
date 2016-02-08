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

package jtransc.security.provider;

import jtransc.annotation.JTranscInvisible;
import jtransc.JTranscBits;

@JTranscInvisible
public final class MD5 extends DigestBase {
    private int[] state = new int[4];
    private int[] x = new int[16];

    public MD5() {
        super("MD5", 16, 64);
        this.implReset();
    }

    public Object clone() throws CloneNotSupportedException {
        MD5 var1 = (MD5) super.clone();
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
        long var3 = this.bytesProcessed << 3;
        int var5 = (int) this.bytesProcessed & 63;
        int var6 = var5 < 56 ? 56 - var5 : 120 - var5;
        this.engineUpdate(padding, 0, var6);
        JTranscBits.i2bLittle4((int) var3, this.buffer, 56);
        JTranscBits.i2bLittle4((int) (var3 >>> 32), this.buffer, 60);
        this.implCompress(this.buffer, 0);
        JTranscBits.i2bLittle(this.state, 0, data, offset, 16);
    }

    private static int FF(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        var0 += (var1 & var2 | ~var1 & var3) + var4 + var6;
        return (var0 << var5 | var0 >>> 32 - var5) + var1;
    }

    private static int GG(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        var0 += (var1 & var3 | var2 & ~var3) + var4 + var6;
        return (var0 << var5 | var0 >>> 32 - var5) + var1;
    }

    private static int HH(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        var0 += (var1 ^ var2 ^ var3) + var4 + var6;
        return (var0 << var5 | var0 >>> 32 - var5) + var1;
    }

    private static int II(int var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        var0 += (var2 ^ (var1 | ~var3)) + var4 + var6;
        return (var0 << var5 | var0 >>> 32 - var5) + var1;
    }

    void implCompress(byte[] data, int offset) {
        JTranscBits.b2iLittle64(data, offset, this.x);
        int var3 = this.state[0];
        int var4 = this.state[1];
        int var5 = this.state[2];
        int var6 = this.state[3];
        var3 = FF(var3, var4, var5, var6, this.x[0], 7, -680876936);
        var6 = FF(var6, var3, var4, var5, this.x[1], 12, -389564586);
        var5 = FF(var5, var6, var3, var4, this.x[2], 17, 606105819);
        var4 = FF(var4, var5, var6, var3, this.x[3], 22, -1044525330);
        var3 = FF(var3, var4, var5, var6, this.x[4], 7, -176418897);
        var6 = FF(var6, var3, var4, var5, this.x[5], 12, 1200080426);
        var5 = FF(var5, var6, var3, var4, this.x[6], 17, -1473231341);
        var4 = FF(var4, var5, var6, var3, this.x[7], 22, -45705983);
        var3 = FF(var3, var4, var5, var6, this.x[8], 7, 1770035416);
        var6 = FF(var6, var3, var4, var5, this.x[9], 12, -1958414417);
        var5 = FF(var5, var6, var3, var4, this.x[10], 17, -42063);
        var4 = FF(var4, var5, var6, var3, this.x[11], 22, -1990404162);
        var3 = FF(var3, var4, var5, var6, this.x[12], 7, 1804603682);
        var6 = FF(var6, var3, var4, var5, this.x[13], 12, -40341101);
        var5 = FF(var5, var6, var3, var4, this.x[14], 17, -1502002290);
        var4 = FF(var4, var5, var6, var3, this.x[15], 22, 1236535329);
        var3 = GG(var3, var4, var5, var6, this.x[1], 5, -165796510);
        var6 = GG(var6, var3, var4, var5, this.x[6], 9, -1069501632);
        var5 = GG(var5, var6, var3, var4, this.x[11], 14, 643717713);
        var4 = GG(var4, var5, var6, var3, this.x[0], 20, -373897302);
        var3 = GG(var3, var4, var5, var6, this.x[5], 5, -701558691);
        var6 = GG(var6, var3, var4, var5, this.x[10], 9, 38016083);
        var5 = GG(var5, var6, var3, var4, this.x[15], 14, -660478335);
        var4 = GG(var4, var5, var6, var3, this.x[4], 20, -405537848);
        var3 = GG(var3, var4, var5, var6, this.x[9], 5, 568446438);
        var6 = GG(var6, var3, var4, var5, this.x[14], 9, -1019803690);
        var5 = GG(var5, var6, var3, var4, this.x[3], 14, -187363961);
        var4 = GG(var4, var5, var6, var3, this.x[8], 20, 1163531501);
        var3 = GG(var3, var4, var5, var6, this.x[13], 5, -1444681467);
        var6 = GG(var6, var3, var4, var5, this.x[2], 9, -51403784);
        var5 = GG(var5, var6, var3, var4, this.x[7], 14, 1735328473);
        var4 = GG(var4, var5, var6, var3, this.x[12], 20, -1926607734);
        var3 = HH(var3, var4, var5, var6, this.x[5], 4, -378558);
        var6 = HH(var6, var3, var4, var5, this.x[8], 11, -2022574463);
        var5 = HH(var5, var6, var3, var4, this.x[11], 16, 1839030562);
        var4 = HH(var4, var5, var6, var3, this.x[14], 23, -35309556);
        var3 = HH(var3, var4, var5, var6, this.x[1], 4, -1530992060);
        var6 = HH(var6, var3, var4, var5, this.x[4], 11, 1272893353);
        var5 = HH(var5, var6, var3, var4, this.x[7], 16, -155497632);
        var4 = HH(var4, var5, var6, var3, this.x[10], 23, -1094730640);
        var3 = HH(var3, var4, var5, var6, this.x[13], 4, 681279174);
        var6 = HH(var6, var3, var4, var5, this.x[0], 11, -358537222);
        var5 = HH(var5, var6, var3, var4, this.x[3], 16, -722521979);
        var4 = HH(var4, var5, var6, var3, this.x[6], 23, 76029189);
        var3 = HH(var3, var4, var5, var6, this.x[9], 4, -640364487);
        var6 = HH(var6, var3, var4, var5, this.x[12], 11, -421815835);
        var5 = HH(var5, var6, var3, var4, this.x[15], 16, 530742520);
        var4 = HH(var4, var5, var6, var3, this.x[2], 23, -995338651);
        var3 = II(var3, var4, var5, var6, this.x[0], 6, -198630844);
        var6 = II(var6, var3, var4, var5, this.x[7], 10, 1126891415);
        var5 = II(var5, var6, var3, var4, this.x[14], 15, -1416354905);
        var4 = II(var4, var5, var6, var3, this.x[5], 21, -57434055);
        var3 = II(var3, var4, var5, var6, this.x[12], 6, 1700485571);
        var6 = II(var6, var3, var4, var5, this.x[3], 10, -1894986606);
        var5 = II(var5, var6, var3, var4, this.x[10], 15, -1051523);
        var4 = II(var4, var5, var6, var3, this.x[1], 21, -2054922799);
        var3 = II(var3, var4, var5, var6, this.x[8], 6, 1873313359);
        var6 = II(var6, var3, var4, var5, this.x[15], 10, -30611744);
        var5 = II(var5, var6, var3, var4, this.x[6], 15, -1560198380);
        var4 = II(var4, var5, var6, var3, this.x[13], 21, 1309151649);
        var3 = II(var3, var4, var5, var6, this.x[4], 6, -145523070);
        var6 = II(var6, var3, var4, var5, this.x[11], 10, -1120210379);
        var5 = II(var5, var6, var3, var4, this.x[2], 15, 718787259);
        var4 = II(var4, var5, var6, var3, this.x[9], 21, -343485551);
        this.state[0] += var3;
        this.state[1] += var4;
        this.state[2] += var5;
        this.state[3] += var6;
    }
}
