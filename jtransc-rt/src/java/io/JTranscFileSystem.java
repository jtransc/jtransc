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

class JTranscFileSystem extends FileSystem {

	private final char slash;
	private final char colon;
	private final String javaHome;

	public JTranscFileSystem() {
		slash = System.getProperty("file.separator").charAt(0);
		colon = System.getProperty("path.separator").charAt(0);
		javaHome = System.getProperty("java.home");
	}

	public char getSeparator() {
		return slash;
	}

	public char getPathSeparator() {
		return colon;
	}

	private String normalize(String pathname, int len, int off) {
		if (len == 0) return pathname;
		int n = len;
		while ((n > 0) && (pathname.charAt(n - 1) == '/')) n--;
		if (n == 0) return "/";
		StringBuffer sb = new StringBuffer(pathname.length());
		if (off > 0) sb.append(pathname.substring(0, off));
		char prevChar = 0;
		for (int i = off; i < n; i++) {
			char c = pathname.charAt(i);
			if ((prevChar == '/') && (c == '/')) continue;
			sb.append(c);
			prevChar = c;
		}
		return sb.toString();
	}

	public String normalize(String pathname) {
		int n = pathname.length();
		char prevChar = 0;
		for (int i = 0; i < n; i++) {
			char c = pathname.charAt(i);
			if ((prevChar == '/') && (c == '/'))
				return normalize(pathname, n, i - 1);
			prevChar = c;
		}
		if (prevChar == '/') return normalize(pathname, n, n - 1);
		return pathname;
	}

	public int prefixLength(String pathname) {
		if (pathname.length() == 0) return 0;
		return (pathname.charAt(0) == '/') ? 1 : 0;
	}

	public String resolve(String parent, String child) {
		if (child.equals("")) return parent;
		if (child.charAt(0) == '/') {
			if (parent.equals("/")) return child;
			return parent + child;
		}
		if (parent.equals("/")) return parent + child;
		return parent + '/' + child;
	}

	public String getDefaultParent() {
		return "/";
	}

	public String fromURIPath(String path) {
		String p = path;
		if (p.endsWith("/") && (p.length() > 1)) {
			// "/foo/" --> "/foo", but "/" --> "/"
			p = p.substring(0, p.length() - 1);
		}
		return p;
	}

	public boolean isAbsolute(File f) {
		return (f.getPrefixLength() != 0);
	}

	public String resolve(File f) {
		if (isAbsolute(f)) return f.getPath();
		return resolve(System.getProperty("user.dir"), f.getPath());
	}

	native public String canonicalize(String path) throws IOException;

	static String parentOrNull(String path) {
		if (path == null) return null;
		char sep = File.separatorChar;
		int last = path.length() - 1;
		int idx = last;
		int adjacentDots = 0;
		int nonDotCount = 0;
		while (idx > 0) {
			char c = path.charAt(idx);
			if (c == '.') {
				if (++adjacentDots >= 2) return null;
			} else if (c == sep) {
				if (adjacentDots == 1 && nonDotCount == 0) return null;
				if (idx == 0 || idx >= last - 1 || path.charAt(idx - 1) == sep) return null;
				return path.substring(0, idx);
			} else {
				++nonDotCount;
				adjacentDots = 0;
			}
			--idx;
		}
		return null;
	}

	public native int getBooleanAttributes0(File f);

	public int getBooleanAttributes(File f) {
		int rv = getBooleanAttributes0(f);
		String name = f.getName();
		boolean hidden = (name.length() > 0) && (name.charAt(0) == '.');
		return rv | (hidden ? BA_HIDDEN : 0);
	}

	public native boolean checkAccess(File f, int access);

	public native long getLastModifiedTime(File f);

	public native long getLength(File f);

	public native boolean setPermission(File f, int access, boolean enable, boolean owneronly);

	public native boolean createFileExclusively(String path) throws IOException;

	native public boolean delete(File f);

	public native String[] list(File f);

	public native boolean createDirectory(File f);

	native public boolean rename(File f1, File f2);

	public native boolean setLastModifiedTime(File f, long time);

	public native boolean setReadOnly(File f);

	public File[] listRoots() {
		return new File[]{new File("/")};
	}

	public native long getSpace(File f, int t);

	public int compare(File f1, File f2) {
		return f1.getPath().compareTo(f2.getPath());
	}

	public int hashCode(File f) {
		return f.getPath().hashCode() ^ 1234321;
	}
}
