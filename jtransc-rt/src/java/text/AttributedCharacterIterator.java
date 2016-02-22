/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface AttributedCharacterIterator extends CharacterIterator {
	class Attribute implements Serializable {
		private String name;
		private static final Map<String, Attribute> instances = new HashMap<String, Attribute>(7);

		protected Attribute(String name) {
			this.name = name;
			instances.put(name, this);
		}

		public final boolean equals(Object obj) {
			return super.equals(obj);
		}

		public final int hashCode() {
			return super.hashCode();
		}

		public String toString() {
			return getClass().getName() + "(" + name + ")";
		}

		protected String getName() {
			return name;
		}

		protected Object readResolve() throws InvalidObjectException {
			return instances.get(getName());
		}

		public static final Attribute LANGUAGE = new Attribute("language");
		public static final Attribute READING = new Attribute("reading");
		public static final Attribute INPUT_METHOD_SEGMENT = new Attribute("input_method_segment");
	}

	int getRunStart();

	int getRunStart(Attribute attribute);

	int getRunStart(Set<? extends Attribute> attributes);

	int getRunLimit();

	int getRunLimit(Attribute attribute);

	int getRunLimit(Set<? extends Attribute> attributes);

	Map<Attribute, Object> getAttributes();

	Object getAttribute(Attribute attribute);

	Set<Attribute> getAllAttributeKeys();
}
