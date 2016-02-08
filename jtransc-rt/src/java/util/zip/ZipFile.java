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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;

public class ZipFile implements ZipConstants, Closeable {
    public static final int OPEN_READ = 0x1;
    public static final int OPEN_DELETE = 0x4;

    public ZipFile(String name) throws IOException {
        this(new File(name), OPEN_READ);
    }

    public ZipFile(File file, int mode) throws IOException {
        this(file, mode, Charset.forName("UTF-8"));
    }

    public ZipFile(File file) throws ZipException, IOException {
        this(file, OPEN_READ);
    }

    public ZipFile(File file, int mode, Charset charset) throws IOException {
    }

    public ZipFile(String name, Charset charset) throws IOException {
        this(new File(name), OPEN_READ, charset);
    }

    public ZipFile(File file, Charset charset) throws IOException {
        this(file, OPEN_READ, charset);
    }

    native public String getComment();

    native public ZipEntry getEntry(String name);

    native public InputStream getInputStream(ZipEntry entry) throws IOException;

    native public String getName();

    native public Enumeration<? extends ZipEntry> entries();

    native public int size();

    native public void close() throws IOException;

    protected void finalize() throws IOException {
        close();
    }
}
