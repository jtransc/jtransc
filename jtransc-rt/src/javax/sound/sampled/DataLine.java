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

public interface DataLine extends Line {
	void drain();

	void flush();

	void start();

	void stop();

	boolean isRunning();

	boolean isActive();

	AudioFormat getFormat();

	int getBufferSize();

	int available();

	int getFramePosition();

	long getLongFramePosition();

	long getMicrosecondPosition();

	float getLevel();

	class Info extends Line.Info {
		public Info(Class<?> lineClass, AudioFormat[] formats, int minBufferSize, int maxBufferSize) {
			super(lineClass);
			throw new RuntimeException("Not implemented");
		}

		public Info(Class<?> lineClass, AudioFormat format, int bufferSize) {
			super(lineClass);
			throw new RuntimeException("Not implemented");
		}

		public Info(Class<?> lineClass, AudioFormat format) {
			this(lineClass, format, AudioSystem.NOT_SPECIFIED);
		}

		native public AudioFormat[] getFormats();

		native public boolean isFormatSupported(AudioFormat format);

		native public int getMinBufferSize();

		native public int getMaxBufferSize();

		native public boolean matches(Line.Info info);

		native public String toString();
	}
}
