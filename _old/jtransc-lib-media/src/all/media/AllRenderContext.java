package all.media;

public interface AllRenderContext {
    AllRenderProgram getSolidColorProgram();
    AllRenderProgram getTextureProgram();
    AllRenderTexture createTexture(AllTextureSource source);
    IndexBuffer createIndexBuffer(int numIndices);
    VertexBuffer createVertexBuffer(int numVertices, int data32PerVertex);
    void drawTriangles(AllRenderTexture texture, AllRenderProgram program, IndexBuffer indices, VertexBuffer vertices, int indexStart, int count);
}
