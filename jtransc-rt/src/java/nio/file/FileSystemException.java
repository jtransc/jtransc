/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package java.nio.file;

import java.io.IOException;

public class FileSystemException extends IOException {
	private final String file;
	private final String other;

	public FileSystemException(String file) {
		super((String)null);
		this.file = file;
		this.other = null;
	}

	public FileSystemException(String file, String other, String reason) {
		super(reason);
		this.file = file;
		this.other = other;
	}

	public String getFile() {
		return file;
	}
	public String getOtherFile() {
		return other;
	}
	public String getReason() {
		return super.getMessage();
	}

	@Override
	public String getMessage() {
		String out = "";
		if (file != null) out += file;
		if (other != null) {
			out += " -> ";
			out += other;
		}
		if (getReason() != null) {
			if (!out.isEmpty()) out += ": ";
			out += getReason();
		}
		return out;
	}
}
