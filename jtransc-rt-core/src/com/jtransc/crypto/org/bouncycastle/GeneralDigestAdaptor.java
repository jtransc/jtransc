package com.jtransc.crypto.org.bouncycastle;

import java.security.DigestException;
import java.security.MessageDigest;

public class GeneralDigestAdaptor extends MessageDigest {
	final private GeneralDigest generalDigest;

	public GeneralDigestAdaptor(GeneralDigest generalDigest) {
		super(generalDigest.getAlgorithmName());
		this.generalDigest = generalDigest;
	}

	@Override
	protected int engineGetDigestLength() {
		return generalDigest.getDigestSize();
	}

	@Override
	protected void engineUpdate(byte input) {
		generalDigest.update(input);
	}

	@Override
	protected void engineUpdate(byte[] input, int offset, int len) {
		generalDigest.update(input, offset, len);
	}

	@Override
	protected byte[] engineDigest() {
		byte[] out = new byte[engineGetDigestLength()];
		try {
			engineDigest(out, 0, out.length);
		} catch (DigestException e) {
			e.printStackTrace();
			return new byte[0];
		}
		return out;
	}

	@Override
	protected int engineDigest(byte[] buf, int offset, int len) throws DigestException {
		generalDigest.doFinal(buf, offset);
		return engineGetDigestLength();
	}

	@Override
	protected void engineReset() {
		generalDigest.reset();
	}

	@Override
	native public Object clone() throws CloneNotSupportedException;
}
