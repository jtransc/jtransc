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

package java.io;

public abstract class FilterWriter extends Writer {
    protected Writer out;

    protected FilterWriter(Writer out) {
        super(out);
        this.out = out;
    }

    public void write(int value) throws IOException {
        out.write(value);
    }

    public void write(char value[], int offset, int length) throws IOException {
        out.write(value, offset, length);
    }

    public void write(String str, int off, int len) throws IOException {
        out.write(str, off, len);
    }

    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }
}