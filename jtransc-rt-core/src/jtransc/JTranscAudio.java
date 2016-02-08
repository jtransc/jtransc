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

package jtransc;

public class JTranscAudio {
	static public Impl impl;

	static public int createSound(String path) {
		if (impl != null) return impl.createSound(path);
		return -1;
	}

	static public void disposeSound(int soundId) {
		if (impl != null) {
			impl.disposeSound(soundId);
		} else {
			System.out.println("JTranscAudio.disposeSound:" + soundId);
		}
	}

	static public void playSound(int soundId) {
		if (impl != null) {
			impl.playSound(soundId);
		}
	}

	interface Impl {
		int createSound(String path);

		void disposeSound(int soundId);

		void playSound(int soundId);
	}
}
