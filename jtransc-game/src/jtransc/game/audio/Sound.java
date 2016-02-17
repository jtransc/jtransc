package jtransc.game.audio;

import jtransc.JTranscAudio;

/**
 * Use JTranscGame.sound() to create a sound
 */
public class Sound {
	private int id;

	public Sound(String path) {
		id = JTranscAudio.createSound(path);
	}

	public void dispose() {
		if (id < 0) return;
		JTranscAudio.disposeSound(id);
		id = -1;
	}

	public void play() {
		JTranscAudio.playSound(id);
	}
}
