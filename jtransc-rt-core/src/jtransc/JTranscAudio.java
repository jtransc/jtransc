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
	static public Impl impl = new Impl() {
		@Override
		public int createSound(String path) {
			System.out.println("JTranscAudio.createSound:" + path);
			return -1;
		}

		@Override
		public void disposeSound(int soundId) {
			System.out.println("JTranscAudio.disposeSound:" + soundId);
		}

		@Override
		public void playSound(int soundId) {
			System.out.println("JTranscAudio.playSound:" + soundId);
		}
	};

	static public int createSound(String path) {
		return impl.createSound(path);
	}

	static public void disposeSound(int soundId) {
		impl.disposeSound(soundId);
	}

	static public void playSound(int soundId) {
		impl.playSound(soundId);
	}

	public interface Impl {
		int createSound(String path);

		void disposeSound(int soundId);

		void playSound(int soundId);
	}
}
