package java.net.internal;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class HttpHandler extends URLStreamHandler {
	@Override
	public URLConnection openConnection(URL url) throws IOException {
		return new Http(url);
	}

	//protected void parseURL(URL url, String spec, int start, int end) {
	//	//if (this != url.streamHandler) {
	//	//	throw new SecurityException("Only a URL's stream handler is permitted to mutate it");
	//	//}
	//	if (end < start) {
	//		throw new StringIndexOutOfBoundsException(spec, start, end - start);
	//	}
//
	//	int fileStart;
	//	String authority;
	//	String userInfo;
	//	String host;
	//	int port = -1;
	//	String path;
	//	String query;
	//	String ref;
	//	if (spec.regionMatches(start, "//", 0, 2)) {
	//		// Parse the authority from the spec.
	//		int authorityStart = start + 2;
	//		fileStart = UrlUtils.findFirstOf(spec, "/?#", authorityStart, end);
	//		authority = spec.substring(authorityStart, fileStart);
	//		int userInfoEnd = UrlUtils.findFirstOf(spec, "@", authorityStart, fileStart);
	//		int hostStart;
	//		if (userInfoEnd != fileStart) {
	//			userInfo = spec.substring(authorityStart, userInfoEnd);
	//			hostStart = userInfoEnd + 1;
	//		} else {
	//			userInfo = null;
	//			hostStart = authorityStart;
	//		}
//
    //        /*
	//		 * Extract the host and port. The host may be an IPv6 address with
    //         * colons like "[::1]", in which case we look for the port delimiter
    //         * colon after the ']' character.
    //         */
	//		int colonSearchFrom = hostStart;
	//		int ipv6End = UrlUtils.findFirstOf(spec, "]", hostStart, fileStart);
	//		if (ipv6End != fileStart) {
	//			if (UrlUtils.findFirstOf(spec, ":", hostStart, ipv6End) == ipv6End) {
	//				throw new IllegalArgumentException("Expected an IPv6 address: "
	//					+ spec.substring(hostStart, ipv6End + 1));
	//			}
	//			colonSearchFrom = ipv6End;
	//		}
	//		int hostEnd = UrlUtils.findFirstOf(spec, ":", colonSearchFrom, fileStart);
	//		host = spec.substring(hostStart, hostEnd);
	//		int portStart = hostEnd + 1;
	//		if (portStart < fileStart) {
	//			port = Integer.parseInt(spec.substring(portStart, fileStart));
	//			if (port < 0) {
	//				throw new IllegalArgumentException("port < 0: " + port);
	//			}
	//		}
	//		path = null;
	//		query = null;
	//		ref = null;
	//	} else {
	//		// Get the authority from the context URL.
	//		fileStart = start;
	//		authority = url.getAuthority();
	//		userInfo = url.getUserInfo();
	//		host = url.getHost();
	//		if (host == null) {
	//			host = "";
	//		}
	//		port = url.getPort();
	//		path = url.getPath();
	//		query = url.getQuery();
	//		ref = url.getRef();
	//	}
//
    //    /*
    //     * Extract the path, query and fragment. Each part has its own leading
    //     * delimiter character. The query can contain slashes and the fragment
    //     * can contain slashes and question marks.
    //     *    / path ? query # fragment
    //     */
	//	int pos = fileStart;
	//	while (pos < end) {
	//		int nextPos;
	//		switch (spec.charAt(pos)) {
	//			case '#':
	//				nextPos = end;
	//				ref = spec.substring(pos + 1, nextPos);
	//				break;
	//			case '?':
	//				nextPos = UrlUtils.findFirstOf(spec, "#", pos, end);
	//				query = spec.substring(pos + 1, nextPos);
	//				ref = null;
	//				break;
	//			default:
	//				nextPos = UrlUtils.findFirstOf(spec, "?#", pos, end);
	//				path = relativePath(path, spec.substring(pos, nextPos));
	//				query = null;
	//				ref = null;
	//				break;
	//		}
	//		pos = nextPos;
	//	}
//
	//	if (path == null) {
	//		path = "";
	//	}
//
	//	path = UrlUtils.authoritySafePath(authority, path);
//
	//	setURL(url, url.getProtocol(), host, port, authority, userInfo, path, query, ref);
	//}
}
