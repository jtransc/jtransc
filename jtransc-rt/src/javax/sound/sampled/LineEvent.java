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

public class LineEvent extends java.util.EventObject {
	public LineEvent(Line line, Type type, long position) {
		super(line);
		throw new RuntimeException("Not implemented");
	}

	public final Line getLine() {
		return (Line) getSource();
	}

	native public final Type getType();

	native public final long getFramePosition();

	native public String toString();

	public static class Type {
		private final String name;

		protected Type(String name) {
			this.name = name;
		}

		native public final boolean equals(Object obj);

		native public final int hashCode();

		native public String toString();

		public static final Type OPEN = new Type("Open");
		public static final Type CLOSE = new Type("Close");
		public static final Type START = new Type("Start");
		public static final Type STOP = new Type("Stop");
	}
}
