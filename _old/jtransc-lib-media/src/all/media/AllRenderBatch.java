package all.media;

public class AllRenderBatch {
    public AllRenderTexture texture;
    public AllRenderProgram program;
    public int indexStart;
    public int indexEnd;

    public AllRenderBatch(AllRenderTexture texture, AllRenderProgram program, int indexStart, int indexEnd) {
        this.texture = texture;
        this.program = program;
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
    }
}
