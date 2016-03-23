package java.security.cert;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

public abstract class CertPath implements Serializable {
	private String type;

	protected CertPath(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public abstract Iterator<String> getEncodings();

	public String toString() {
		return "CertPath";
	}

	public abstract byte[] getEncoded() throws CertificateEncodingException;

	public abstract byte[] getEncoded(String encoding) throws CertificateEncodingException;

	public abstract List<? extends Certificate> getCertificates();

	native protected Object writeReplace() throws ObjectStreamException;
}
