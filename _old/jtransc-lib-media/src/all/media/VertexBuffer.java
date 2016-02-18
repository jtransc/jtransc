package all.media;

public interface VertexBuffer {
    void upload(float[] data, int start, int count);
    void dispose();
}
