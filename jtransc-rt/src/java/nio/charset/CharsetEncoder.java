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

public abstract class CharsetEncoder {
	protected CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar, byte[] replacement) {
	}

	protected CharsetEncoder(Charset cs, float averageBytesPerChar, float maxBytesPerChar) {
	}

	native public final Charset charset();

	native public final byte[] replacement();

	native public final CharsetEncoder replaceWith(byte[] newReplacement);

	protected void implReplaceWith(byte[] newReplacement) {
	}

	native public boolean isLegalReplacement(byte[] repl);

	native public CodingErrorAction malformedInputAction();

	native public final CharsetEncoder onMalformedInput(CodingErrorAction newAction);

	protected void implOnMalformedInput(CodingErrorAction newAction) {
	}

	native public CodingErrorAction unmappableCharacterAction();

	native public final CharsetEncoder onUnmappableCharacter(CodingErrorAction newAction);

	protected void implOnUnmappableCharacter(CodingErrorAction newAction) {
	}

	native public final float averageBytesPerChar();

	native public final float maxBytesPerChar();

	native public final CoderResult encode(CharBuffer in, ByteBuffer out, boolean endOfInput);

	native public final CoderResult flush(ByteBuffer out);

	protected CoderResult implFlush(ByteBuffer out) {
		return CoderResult.UNDERFLOW;
	}

	native public final CharsetEncoder reset();

	protected void implReset() {
	}

	protected abstract CoderResult encodeLoop(CharBuffer in, ByteBuffer out);

	native public final ByteBuffer encode(CharBuffer in) throws CharacterCodingException;

	native public boolean canEncode(char c);

	native public boolean canEncode(CharSequence cs);
}
