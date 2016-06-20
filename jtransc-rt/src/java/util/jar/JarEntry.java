/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.jar;

import java.io.IOException;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Represents a single file in a JAR archive together with the manifest
 * attributes and digital signatures associated with it.
 *
 * @see JarFile
 * @see JarInputStream
 */
public class JarEntry extends ZipEntry {
	private Attributes attributes;

	JarFile parentJar;

	CodeSigner signers[];

	// Cached factory used to build CertPath-s in <code>getCodeSigners()</code>.

	private boolean isFactoryChecked = false;

	/**
	 * Creates a new {@code JarEntry} named name.
	 *
	 * @param name The name of the new {@code JarEntry}.
	 */
	public JarEntry(String name) {
		super(name);
	}

	/**
	 * Creates a new {@code JarEntry} using the values obtained from entry.
	 *
	 * @param entry The ZipEntry to obtain values from.
	 */
	public JarEntry(ZipEntry entry) {
		super(entry);
	}

	/**
	 * Returns the {@code Attributes} object associated with this entry or
	 * {@code null} if none exists.
	 *
	 * @return the {@code Attributes} for this entry.
	 * @throws IOException If an error occurs obtaining the {@code Attributes}.
	 * @see Attributes
	 */
	public Attributes getAttributes() throws IOException {
		if (attributes != null || parentJar == null) {
			return attributes;
		}
		Manifest manifest = parentJar.getManifest();
		if (manifest == null) {
			return null;
		}
		return attributes = manifest.getAttributes(getName());
	}

	/**
	 * Returns an array of {@code Certificate} Objects associated with this
	 * entry or {@code null} if none exists. Make sure that the everything is
	 * read from the input stream before calling this method, or else the method
	 * returns {@code null}.
	 *
	 * @return the certificate for this entry.
	 * @see java.security.cert.Certificate
	 */
	public Certificate[] getCertificates() {
		return new Certificate[0];
	}

	void setAttributes(Attributes attrib) {
		attributes = attrib;
	}

	/**
	 * Create a new {@code JarEntry} using the values obtained from the
	 * argument.
	 *
	 * @param je The {@code JarEntry} to obtain values from.
	 */
	public JarEntry(JarEntry je) {
		super(je);
		parentJar = je.parentJar;
		attributes = je.attributes;
		signers = je.signers;
	}

	/**
	 * Returns the code signers for the digital signatures associated with the
	 * JAR file. If there is no such code signer, it returns {@code null}. Make
	 * sure that the everything is read from the input stream before calling
	 * this method, or else the method returns {@code null}.
	 *
	 * @return the code signers for the JAR entry.
	 * @see CodeSigner
	 */
	public CodeSigner[] getCodeSigners() {
		if (signers == null) {
			signers = getCodeSigners(getCertificates());
		}
		if (signers == null) {
			return null;
		}

		CodeSigner[] tmp = new CodeSigner[signers.length];
		System.arraycopy(signers, 0, tmp, 0, tmp.length);
		return tmp;
	}

	private CodeSigner[] getCodeSigners(Certificate[] certs) {
		return new CodeSigner[0];

	}

	private void addCodeSigner(ArrayList<CodeSigner> asigners, List<Certificate> list) {
	}
}
