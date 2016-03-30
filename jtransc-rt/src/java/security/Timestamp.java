package java.security;

import java.io.Serializable;
import java.security.cert.CertPath;
import java.util.Date;

public final class Timestamp implements Serializable {
	private Date timestamp;
	private CertPath signerCertPath;

	public Timestamp(Date timestamp, CertPath signerCertPath) {
		this.timestamp = new Date(timestamp.getTime());
		this.signerCertPath = signerCertPath;
	}

	public Date getTimestamp() {
		return new Date(timestamp.getTime());
	}

	public CertPath getSignerCertPath() {
		return signerCertPath;
	}

	public int hashCode() {
		return timestamp.hashCode() + signerCertPath.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null || (!(obj instanceof Timestamp))) return false;
		Timestamp that = (Timestamp) obj;
		if (this == that) return true;
		return (timestamp.equals(that.getTimestamp()) && signerCertPath.equals(that.getSignerCertPath()));
	}
}
