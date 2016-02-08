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

package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public abstract class CharsetDecoder {
	protected CharsetDecoder(Charset cs, float averageCharsPerByte, float maxCharsPerByte) {
	}

	native public final Charset charset();

	native public final String replacement();

	native public final float averageCharsPerByte();

	native public final float maxCharsPerByte();

	native public final CharsetDecoder replaceWith(String newReplacement);

	native public CodingErrorAction malformedInputAction();

	native public final CharsetDecoder onMalformedInput(CodingErrorAction newAction);

	native public CodingErrorAction unmappableCharacterAction();

	native public final CharsetDecoder onUnmappableCharacter(CodingErrorAction newAction);

	protected void implReplaceWith(String newReplacement) {
	}

	protected void implOnMalformedInput(CodingErrorAction newAction) {
	}

	protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
	}

	native public final CoderResult decode(ByteBuffer in, CharBuffer out, boolean endOfInput);

	native public final CoderResult flush(CharBuffer out);

	native protected CoderResult implFlush(CharBuffer out);

	native public final CharsetDecoder reset();

	protected void implReset() {
	}

	protected abstract CoderResult decodeLoop(ByteBuffer in, CharBuffer out);

	native public final CharBuffer decode(ByteBuffer in) throws CharacterCodingException;

	public boolean isAutoDetecting() {
		return false;
	}

	public boolean isCharsetDetected() {
		throw new UnsupportedOperationException();
	}

	public Charset detectedCharset() {
		throw new UnsupportedOperationException();
	}

}
