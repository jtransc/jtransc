import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import jtransc.IntStack;
import jtransc.JTranscAudio;

public class LibgdxAudio implements JTranscAudio.Impl {
	IntStack audioIds = new IntStack(2048);
	Sound[] sounds = new Sound[2048];

	public LibgdxAudio() {
		for (int n = 2047; n >= 0; n--) audioIds.push(n);
	}

	@Override
	public int createSound(String path) {
		int soundId = audioIds.pop();
		FileHandle fileHandle = Gdx.files.internal(path);
		sounds[soundId] = Gdx.audio.newSound(fileHandle);
		return soundId;
	}

	@Override
	public void disposeSound(int soundId) {
		sounds[soundId].dispose();
		sounds[soundId] = null;
		audioIds.push(soundId);
	}

	@Override
	public void playSound(int soundId) {
		sounds[soundId].play();
	}
}
