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

package javax.sound.sampled;

import java.util.Map;

public class AudioFileFormat {
	protected AudioFileFormat(Type type, int byteLength, AudioFormat format, int frameLength) {
		throw new RuntimeException("Not implemented");
	}

	public AudioFileFormat(Type type, AudioFormat format, int frameLength) {
		this(type, AudioSystem.NOT_SPECIFIED, format, frameLength);
	}

	public AudioFileFormat(Type type, AudioFormat format, int frameLength, Map<String, Object> properties) {
		this(type, AudioSystem.NOT_SPECIFIED, format, frameLength);
		throw new RuntimeException("Not implemented");
	}

	native public Type getType();

	native public int getByteLength();

	native public AudioFormat getFormat();

	native public int getFrameLength();

	native public Map<String, Object> properties();

	native public Object getProperty(String key);

	native public String toString();

	public static class Type {
		public static final Type WAVE = new Type("WAVE", "wav");
		public static final Type AU = new Type("AU", "au");
		public static final Type AIFF = new Type("AIFF", "aif");
		public static final Type AIFC = new Type("AIFF-C", "aifc");
		public static final Type SND = new Type("SND", "snd");

		private final String name;
		private final String ext;

		public Type(String name, String ext) {

			this.name = name;
			this.ext = ext;
		}

		public final boolean equals(Object obj) {
			return this == obj;
		}

		public final int hashCode() {
			return name.hashCode();
		}

		public final String toString() {
			return name;
		}

		public String getExtension() {
			return ext;
		}
	}
}
