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

import java.security.DigestException;
import java.security.MessageDigestSpi;
import java.security.ProviderException;

@JTranscInvisible
abstract class DigestBase extends MessageDigestSpi implements Cloneable {
    private byte[] oneByte;
    private final String algorithm;
    private final int digestLength;
    private final int blockSize;
    byte[] buffer;
    private int bufOfs;
    long bytesProcessed;
    static final byte[] padding = new byte[136];

    DigestBase(String algorithm, int digestLength, int blockSize) {
        this.algorithm = algorithm;
        this.digestLength = digestLength;
        this.blockSize = blockSize;
        this.buffer = new byte[blockSize];
    }

    protected final int engineGetDigestLength() {
        return this.digestLength;
    }

    protected final void engineUpdate(byte b) {
        if (this.oneByte == null) {
            this.oneByte = new byte[1];
        }

        this.oneByte[0] = b;
        this.engineUpdate(this.oneByte, 0, 1);
    }

    protected final void engineUpdate(byte[] data, int offset, int length) {
        if (length != 0) {
            if (offset >= 0 && length >= 0 && offset <= data.length - length) {
                if (this.bytesProcessed < 0L) {
                    this.engineReset();
                }

                this.bytesProcessed += (long) length;
                if (this.bufOfs != 0) {
                    int var4 = Math.min(length, this.blockSize - this.bufOfs);
                    System.arraycopy(data, offset, this.buffer, this.bufOfs, var4);
                    this.bufOfs += var4;
                    offset += var4;
                    length -= var4;
                    if (this.bufOfs >= this.blockSize) {
                        this.implCompress(this.buffer, 0);
                        this.bufOfs = 0;
                    }
                }

                while (length >= this.blockSize) {
                    this.implCompress(data, offset);
                    length -= this.blockSize;
                    offset += this.blockSize;
                }

                if (length > 0) {
                    System.arraycopy(data, offset, this.buffer, 0, length);
                    this.bufOfs = length;
                }

            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        }
    }

    protected final void engineReset() {
        if (this.bytesProcessed != 0L) {
            this.implReset();
            this.bufOfs = 0;
            this.bytesProcessed = 0L;
        }
    }

    protected final byte[] engineDigest() {
        byte[] data = new byte[this.digestLength];

        try {
            this.engineDigest(data, 0, data.length);
            return data;
        } catch (DigestException var3) {
            throw (ProviderException) (new ProviderException("Internal error")).initCause(var3);
        }
    }

    protected final int engineDigest(byte[] data, int offset, int length) throws DigestException {
        if (length < this.digestLength) {
            throw new DigestException("Length must be at least " + this.digestLength + " for " + this.algorithm + "digests");
        } else if (offset >= 0 && length >= 0 && offset <= data.length - length) {
            if (this.bytesProcessed < 0L) {
                this.engineReset();
            }

            this.implDigest(data, offset);
            this.bytesProcessed = -1L;
            return this.digestLength;
        } else {
            throw new DigestException("Buffer too short to store digest");
        }
    }

    abstract void implCompress(byte[] data, int offset);

    abstract void implDigest(byte[] data, int offset);

    abstract void implReset();

    public Object clone() throws CloneNotSupportedException {
        DigestBase var1 = (DigestBase) super.clone();
        var1.buffer = (byte[]) var1.buffer.clone();
        return var1;
    }

    static {
        padding[0] = -128;
    }
}
