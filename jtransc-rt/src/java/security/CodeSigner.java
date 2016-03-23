package java.security;

import java.io.Serializable;
import java.security.cert.CertPath;
import java.util.Objects;

public final class CodeSigner implements Serializable {
	private CertPath signerCertPath;
	private Timestamp timestamp;
	private transient int myhash = -1;

	public CodeSigner(CertPath signerCertPath, Timestamp timestamp) {
		Objects.requireNonNull(signerCertPath);
		this.signerCertPath = signerCertPath;
		this.timestamp = timestamp;
	}

	public CertPath getSignerCertPath() {
		return signerCertPath;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String toString() {
		return "CodeSigner";
	}

}
