import lime.audio.AudioSource;
import lime.Assets;

class HaxeLimeAudio {
    private var ids:Array<Int>;
    private var sources:Array<AudioSource>;

    public function new() {
        ids = [for (i in 1...1024) i];
        sources = [for (i in 1...1024) null];
    }

    static private var instance:HaxeLimeAudio;

    static private function getInstance() {
        if (instance == null) instance = new HaxeLimeAudio();
        return instance;
    }


    static public function createSound(path:String) {
        var instance = getInstance();
        var soundId = instance.ids.pop();
        instance.sources[soundId] = new AudioSource(Assets.getAudioBuffer(path));
        return soundId;
    }

    static public function disposeSound(soundId:Int) {
        var instance = getInstance();
        instance.sources[soundId].dispose();
        instance.sources[soundId] = null;
        instance.ids.push(soundId);
    }

    static public function playSound(soundId:Int) {
        var instance = getInstance();
        instance.sources[soundId].play();
    }
}