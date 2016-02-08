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

import java.lang.annotation.Native;

abstract class FileSystem {
    public abstract char getSeparator();

    public abstract char getPathSeparator();

    public abstract String normalize(String path);

    public abstract int prefixLength(String path);

    public abstract String resolve(String parent, String child);

    public abstract String getDefaultParent();

    public abstract String fromURIPath(String path);

    public abstract boolean isAbsolute(File f);

    public abstract String resolve(File f);

    public abstract String canonicalize(String path) throws IOException;

    @Native
    public static final int BA_EXISTS = 0x01;
    @Native
    public static final int BA_REGULAR = 0x02;
    @Native
    public static final int BA_DIRECTORY = 0x04;
    @Native
    public static final int BA_HIDDEN = 0x08;

    public abstract int getBooleanAttributes(File f);

    @Native
    public static final int ACCESS_READ = 0x04;
    @Native
    public static final int ACCESS_WRITE = 0x02;
    @Native
    public static final int ACCESS_EXECUTE = 0x01;

    public abstract boolean checkAccess(File f, int access);

    public abstract boolean setPermission(File f, int access, boolean enable, boolean owneronly);

    public abstract long getLastModifiedTime(File f);

    public abstract long getLength(File f);

    public abstract boolean createFileExclusively(String pathname) throws IOException;

    public abstract boolean delete(File f);

    public abstract String[] list(File f);

    public abstract boolean createDirectory(File f);

    public abstract boolean rename(File f1, File f2);

    public abstract boolean setLastModifiedTime(File f, long time);

    public abstract boolean setReadOnly(File f);

    public abstract File[] listRoots();

    @Native
    public static final int SPACE_TOTAL = 0;
    @Native
    public static final int SPACE_FREE = 1;
    @Native
    public static final int SPACE_USABLE = 2;

    public abstract long getSpace(File f, int t);

    public abstract int compare(File f1, File f2);

    public abstract int hashCode(File f);

    static boolean useCanonCaches      = true;
    static boolean useCanonPrefixCache = true;
}
