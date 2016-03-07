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

import java.io.Serializable;

public abstract class Format implements Serializable, Cloneable {
	protected Format() {
	}

	public final String format(Object obj) {
		return format(obj, new StringBuffer(), new FieldPosition(0)).toString();
	}

	public abstract StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos);

	native public AttributedCharacterIterator formatToCharacterIterator(Object obj);

	public abstract Object parseObject(String source, ParsePosition pos);

	public Object parseObject(String source) throws ParseException {
		ParsePosition position = new ParsePosition(0);
		Object result = parseObject(source, position);
		if (position.index == 0) throw new ParseException("Format.parseObject(String) failed", position.errorIndex);
		return result;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public static class Field extends AttributedCharacterIterator.Attribute {
		protected Field(String name) {
			super(name);
		}
	}

	interface FieldDelegate {
		void formatted(Format.Field attr, Object value, int start, int end, StringBuffer buffer);

		void formatted(int fieldID, Format.Field attr, Object value, int start, int end, StringBuffer buffer);
	}
}
