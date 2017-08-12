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

public class AudioFormat {
	protected Encoding encoding;
	protected float sampleRate;
	protected int sampleSizeInBits;
	protected int channels;
	protected int frameSize;
	protected float frameRate;
	protected boolean bigEndian;

	public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
		throw new RuntimeException("Not implemented");
	}

	public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
		throw new RuntimeException("Not implemented");
	}

	public AudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) {
		throw new RuntimeException("Not implemented");
	}

	native public Encoding getEncoding();

	native public float getSampleRate();

	native public int getSampleSizeInBits();

	native public int getChannels();

	native public int getFrameSize();

	native public float getFrameRate();

	native public boolean isBigEndian();

	native public Map<String, Object> properties();

	native public Object getProperty(String key);

	native public boolean matches(AudioFormat format);

	native public String toString();

	public static class Encoding {
		public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");
		public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");
		public static final Encoding PCM_FLOAT = new Encoding("PCM_FLOAT");
		public static final Encoding ULAW = new Encoding("ULAW");
		public static final Encoding ALAW = new Encoding("ALAW");

		private final String name;

		public Encoding(String name) {
			this.name = name;
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
	}
}
