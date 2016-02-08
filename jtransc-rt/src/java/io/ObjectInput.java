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

public interface ObjectInput extends DataInput, AutoCloseable {
    Object readObject() throws ClassNotFoundException, IOException;

    int read() throws IOException;

    int read(byte b[]) throws IOException;

    int read(byte b[], int off, int len) throws IOException;

    long skip(long n) throws IOException;

    int available() throws IOException;

    void close() throws IOException;
}
