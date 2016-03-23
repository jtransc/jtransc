package java.security;

import java.net.URL;

public class CodeSource implements java.io.Serializable {
	public CodeSource(URL url, java.security.cert.Certificate certs[]) {
	}

	public CodeSource(URL url, CodeSigner[] signers) {
	}

	public final URL getLocation() {
		return null;
	}

	public final java.security.cert.Certificate[] getCertificates() {
		return new java.security.cert.Certificate[0];
	}

	public final CodeSigner[] getCodeSigners() {
		return new CodeSigner[0];
	}

	public boolean implies(CodeSource codesource) {
		return true;
	}


	@Override
	public String toString() {
		return "CodeSource";
	}

}
