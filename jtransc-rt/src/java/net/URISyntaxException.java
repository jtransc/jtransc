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

package java.net;

public class URISyntaxException extends Exception {
	private String input;
	private int index;

	public URISyntaxException(String input, String reason, int index) {
		super(reason);

		if (input == null) {
			throw new NullPointerException("input == null");
		} else if (reason == null) {
			throw new NullPointerException("reason == null");
		}

		if (index < -1) {
			throw new IllegalArgumentException("Bad index: " + index);
		}

		this.input = input;
		this.index = index;
	}

	public URISyntaxException(String input, String reason) {
		super(reason);

		if (input == null) {
			throw new NullPointerException("input == null");
		} else if (reason == null) {
			throw new NullPointerException("reason == null");
		}

		this.input = input;
		index = -1;
	}

	public int getIndex() {
		return index;
	}

	public String getReason() {
		return super.getMessage();
	}

	public String getInput() {
		return input;
	}

	@Override
	public String getMessage() {
		String reason = super.getMessage();
		if (index != -1) {
			return reason + " at index " + index + ": " + input;
		}
		return reason + ": " + input;
	}
}
