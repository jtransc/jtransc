package all.media;

public class AllRenderTexture {
    final public AllTextureSource textureSource;

    public AllRenderTexture(AllTextureSource textureSource) {
        this.textureSource = textureSource;
    }

    public int getWidth() { return this.textureSource.getWidth(); }
    public int getHeight() { return this.textureSource.getHeight(); }

    public void dispose() {
    }
}
