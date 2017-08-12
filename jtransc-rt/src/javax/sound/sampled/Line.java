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

public interface Line extends AutoCloseable {
	Line.Info getLineInfo();

	void open() throws LineUnavailableException;

	void close();

	boolean isOpen();

	Control[] getControls();

	boolean isControlSupported(Control.Type control);

	Control getControl(Control.Type control);

	void addLineListener(LineListener listener);

	void removeLineListener(LineListener listener);

	class Info {
		public Info(Class<?> lineClass) {
			throw new RuntimeException("Not implemented");
		}

		native public Class<?> getLineClass();

		native public boolean matches(Info info);

		native public String toString();
	}
}
