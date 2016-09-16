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

import com.jtransc.JTranscSystemProperties;
import com.jtransc.io.JTranscSyncIO;

// @TODO: Move this to something JTransc can replace in jtransc-rt-core!
class JTranscFileSystem extends FileSystem {

	private final char slash = JTranscSystemProperties.fileSeparator().charAt(0);
	private final char colon = JTranscSystemProperties.pathSeparator().charAt(0);
	private final String javaHome = JTranscSystemProperties.javaHome();

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
			sb.append((c == '/') ? slash : c);
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

	public boolean isAbsolute(String path) {
		return JTranscSyncIO.impl.isAbsolute(path);
	}

	public String resolve(String parent, String child) {
		if (isAbsolute(child)) return child;
		return parent + slash + child;
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
		String path = f.getPath();
		if (path.length() >= 2 && path.charAt(1) == ':') return true;
		return (f.getPrefixLength() != 0);
	}

	public String resolve(File f) {
		if (isAbsolute(f)) return normalize2(f.getPath());
		return normalize2(resolve(normalize2(JTranscSyncIO.impl.getCwd()), f.getPath()));
	}

	private String normalize2(String path) {
		return JTranscSyncIO.impl.normalizePath(path);
	}

	public String canonicalize(String path) throws IOException {
		return normalize2(path);
	}

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

	public int getBooleanAttributes0(File f) {
		return JTranscSyncIO.impl.getBooleanAttributes(normalize2(f));
	}

	public int getBooleanAttributes(File f) {
		int rv = getBooleanAttributes0(f);
		String name = f.getName();
		boolean hidden = (name.length() > 0) && (name.charAt(0) == '.');
		return rv | (hidden ? BA_HIDDEN : 0);
	}

	public boolean checkAccess(File f, int access) {
		return JTranscSyncIO.impl.checkAccess(normalize2(f), access);
	}

	public long getLastModifiedTime(File f) {
		return JTranscSyncIO.impl.getLastModifiedTime(normalize2(f));
	}

	public long getLength(File f) {
		//System.out.println(f.getPath());
		//System.out.println(f.getAbsolutePath());
		return JTranscSyncIO.impl.getLength(normalize2(f));
	}

	public boolean setPermission(File f, int access, boolean enable, boolean owneronly) {
		return JTranscSyncIO.impl.setPermission(normalize2(f), access, enable, owneronly);
	}

	private String normalize2(File file) {
		return normalize2(file.getAbsolutePath());
	}

	public boolean createFileExclusively(String path) throws IOException {
		return JTranscSyncIO.impl.createFileExclusively(normalize2(path));
	}

	public boolean delete(File f) {
		return JTranscSyncIO.impl.delete(normalize2(f));
	}

	public String[] list(File f) {
		return JTranscSyncIO.impl.list(normalize2(f));
	}

	public boolean createDirectory(File f) {
		return JTranscSyncIO.impl.createDirectory(normalize2(f));
	}

	public boolean rename(File f1, File f2) {
		return JTranscSyncIO.impl.rename(normalize2(f1), normalize2(f2));
	}

	public boolean setLastModifiedTime(File f, long time) {
		return JTranscSyncIO.impl.setLastModifiedTime(normalize2(f), time);
	}

	public boolean setReadOnly(File f) {
		return JTranscSyncIO.impl.setReadOnly(normalize2(f));
	}

	public File[] listRoots() {
		return new File[]{new File("/")};
	}

	public long getSpace(File f, int t) {
		switch (t) {
			case FileSystem.SPACE_TOTAL: return JTranscSyncIO.impl.getTotalSpace(normalize2(f));
			case FileSystem.SPACE_FREE: return JTranscSyncIO.impl.getFreeSpace(normalize2(f));
			case FileSystem.SPACE_USABLE: return JTranscSyncIO.impl.getUsableSpace(normalize2(f));
		}
		return 0L;
	}

	public int compare(File f1, File f2) {
		return f1.getPath().compareTo(f2.getPath());
	}

	public int hashCode(File f) {
		return f.getPath().hashCode() ^ 1234321;
	}
}
