package all.media;

abstract public class AllMediaEngine {
    static public AllMediaEngine engine;

    public AllMediaEngine() {
        AllMediaEngine.engine = this;
    }

    abstract public void start(AllMediaHandler init);
}
