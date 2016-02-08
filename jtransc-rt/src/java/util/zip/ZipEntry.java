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

package java.util.zip;

import java.util.Objects;

public class ZipEntry implements ZipConstants, Cloneable {

    String name;        // entry name
    long time = -1;     // last modification time
    long crc = -1;      // crc-32 of entry data
    long size = -1;     // uncompressed size of entry data
    long csize = -1;    // compressed size of entry data
    int method = -1;    // compression method
    int flag = 0;       // general purpose flag
    byte[] extra;       // optional extra field data for entry
    String comment;     // optional comment string for entry

    public static final int STORED = 0;
    public static final int DEFLATED = 8;

    public ZipEntry(String name) {
        Objects.requireNonNull(name, "name");
        if (name.length() > 0xFFFF) throw new IllegalArgumentException("entry name too long");
        this.name = name;
    }

    public ZipEntry(ZipEntry e) {
        Objects.requireNonNull(e, "entry");
        name = e.name;
        time = e.time;
        crc = e.crc;
        size = e.size;
        csize = e.csize;
        method = e.method;
        flag = e.flag;
        extra = e.extra;
        comment = e.comment;
    }

    ZipEntry() {
    }

    public String getName() {
        return name;
    }

    native public void setTime(long time);

    native public long getTime();

    public void setSize(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("invalid entry size");
        }
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public long getCompressedSize() {
        return csize;
    }

    public void setCompressedSize(long csize) {
        this.csize = csize;
    }

    public void setCrc(long crc) {
        if (crc < 0 || crc > 0xFFFFFFFFL) throw new IllegalArgumentException("invalid entry crc-32");
        this.crc = crc;
    }

    public long getCrc() {
        return crc;
    }

    public void setMethod(int method) {
        if (method != STORED && method != DEFLATED) throw new IllegalArgumentException("invalid compression method");
        this.method = method;
    }

    public int getMethod() {
        return method;
    }

    native public void setExtra(byte[] extra);

    public byte[] getExtra() {
        return extra;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public boolean isDirectory() {
        return name.endsWith("/");
    }

    public String toString() {
        return getName();
    }

    public int hashCode() {
        return name.hashCode();
    }

    public Object clone() {
        try {
            ZipEntry e = (ZipEntry) super.clone();
            e.extra = (extra == null) ? null : extra.clone();
            return e;
        } catch (CloneNotSupportedException e) {
            // This should never happen, since we are Cloneable
            throw new InternalError(e);
        }
    }
}
