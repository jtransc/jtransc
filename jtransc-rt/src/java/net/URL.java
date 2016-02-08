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

package java.net;

import java.io.IOException;
import java.io.InputStream;

public final class URL implements java.io.Serializable {
	private String protocol;
	private String host;
	private int port = -1;
	private String file;
	private transient String query;
	private String authority;
	private transient String path;
	private transient String userInfo;
	private String ref;
	transient InetAddress hostAddress;
	transient URLStreamHandler handler;
	private int hashCode = -1;

	public URL(String protocol, String host, int port, String file) throws MalformedURLException {
		this(protocol, host, port, file, null);
	}

	public URL(String protocol, String host, String file) throws MalformedURLException {
		this(protocol, host, -1, file);
	}

	public URL(String protocol, String host, int port, String file, URLStreamHandler handler) throws MalformedURLException {
		protocol = protocol.toLowerCase();
		this.protocol = protocol;
		if (host != null) {
			if (host.indexOf(':') >= 0 && !host.startsWith("[")) host = "[" + host + "]";
			this.host = host;
			if (port < -1) throw new MalformedURLException("Invalid port number :" + port);
			this.port = port;
			authority = (port == -1) ? host : host + ":" + port;
		}

		Parts parts = new Parts(file);
		path = parts.getPath();
		query = parts.getQuery();
		this.file = (query != null) ? (path + "?" + query) : path;
		ref = parts.getRef();

		if (handler == null && (handler = getURLStreamHandler(protocol)) == null) {
			throw new MalformedURLException("unknown protocol: " + protocol);
		}
		this.handler = handler;
	}

	public URL(String spec) throws MalformedURLException {
		this(null, spec);
	}

	public URL(URL context, String spec) throws MalformedURLException {
		this(context, spec, null);
	}

	public URL(URL context, String spec, URLStreamHandler handler) throws MalformedURLException {
		String original = spec;
		int i, limit, c;
		int start = 0;
		String newProtocol = null;
		boolean aRef = false;
		boolean isRelative = false;

		try {
			limit = spec.length();
			while ((limit > 0) && (spec.charAt(limit - 1) <= ' ')) limit--; //eliminate trailing whitespace
			while ((start < limit) && (spec.charAt(start) <= ' ')) start++; // eliminate leading whitespace

			if (spec.regionMatches(true, start, "url:", 0, 4)) start += 4;
			if (start < spec.length() && spec.charAt(start) == '#') aRef = true;
			for (i = start; !aRef && (i < limit) && ((c = spec.charAt(i)) != '/'); i++) {
				if (c != ':') continue;
				String s = spec.substring(start, i).toLowerCase();
				if (isValidProtocol(s)) {
					newProtocol = s;
					start = i + 1;
				}
				break;
			}

			// Only use our context if the protocols match.
			protocol = newProtocol;
			if ((context != null) && ((newProtocol == null) || newProtocol.equalsIgnoreCase(context.protocol))) {
				if (handler == null) handler = context.handler;

				if (context.path != null && context.path.startsWith("/")) newProtocol = null;

				if (newProtocol == null) {
					protocol = context.protocol;
					authority = context.authority;
					userInfo = context.userInfo;
					host = context.host;
					port = context.port;
					file = context.file;
					path = context.path;
					isRelative = true;
				}
			}

			if (protocol == null) throw new MalformedURLException("no protocol: " + original);

			if (handler == null && (handler = getURLStreamHandler(protocol)) == null) {
				throw new MalformedURLException("unknown protocol: " + protocol);
			}

			this.handler = handler;

			i = spec.indexOf('#', start);
			if (i >= 0) {
				ref = spec.substring(i + 1, limit);
				limit = i;
			}
			if (isRelative && start == limit) {
				query = context.query;
				if (ref == null) ref = context.ref;
			}
			handler.parseURL(this, spec, start, limit);

		} catch (MalformedURLException e) {
			throw e;
		} catch (Exception e) {
			MalformedURLException exception = new MalformedURLException(e.getMessage());
			exception.initCause(e);
			throw exception;
		}
	}

	private boolean isValidProtocol(String protocol) {
		int len = protocol.length();
		if (len < 1) return false;
		char c = protocol.charAt(0);
		if (!Character.isLetter(c)) return false;
		for (int i = 1; i < len; i++) {
			c = protocol.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '.' && c != '+' && c != '-') return false;
		}
		return true;
	}

	void set(String protocol, String host, int port, String file, String ref) {
		synchronized (this) {
			this.protocol = protocol;
			this.host = host;
			authority = port == -1 ? host : host + ":" + port;
			this.port = port;
			this.file = file;
			this.ref = ref;
			hashCode = -1;
			int q = file.lastIndexOf('?');
			if (q != -1) {
				query = file.substring(q + 1);
				path = file.substring(0, q);
			} else {
				path = file;
			}
		}
	}

	void set(String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref) {
		synchronized (this) {
			this.protocol = protocol;
			this.host = host;
			this.port = port;
			this.file = query == null ? path : path + "?" + query;
			this.userInfo = userInfo;
			this.path = path;
			this.ref = ref;
			hashCode = -1;
			this.query = query;
			this.authority = authority;
		}
	}

	public String getQuery() {
		return query;
	}

	public String getPath() {
		return path;
	}

	public String getUserInfo() {
		return userInfo;
	}

	public String getAuthority() {
		return authority;
	}

	public int getPort() {
		return port;
	}

	public int getDefaultPort() {
		return handler.getDefaultPort();
	}

	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}

	public String getFile() {
		return file;
	}

	public String getRef() {
		return ref;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof URL)) return false;
		URL that = (URL) obj;
		return handler.equals(this, that);
	}

	public synchronized int hashCode() {
		if (hashCode != -1) return hashCode;
		return handler.hashCode(this);
	}

	public boolean sameFile(URL other) {
		return handler.sameFile(this, other);
	}

	public String toString() {
		return toExternalForm();
	}

	public String toExternalForm() {
		return handler.toExternalForm(this);
	}

	public URI toURI() throws URISyntaxException {
		return new URI(toString());
	}

	native static URLStreamHandler getURLStreamHandler(String protocol);

	native public final InputStream openStream() throws IOException;

	class Parts {
		String path, query, ref;

		Parts(String file) {
			int ind = file.indexOf('#');
			ref = ind < 0 ? null : file.substring(ind + 1);
			file = ind < 0 ? file : file.substring(0, ind);
			int q = file.lastIndexOf('?');
			if (q != -1) {
				query = file.substring(q + 1);
				path = file.substring(0, q);
			} else {
				path = file;
			}
		}

		String getPath() {
			return path;
		}

		String getQuery() {
			return query;
		}

		String getRef() {
			return ref;
		}
	}
}

