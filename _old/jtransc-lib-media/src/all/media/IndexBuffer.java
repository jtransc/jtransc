package all.media;

public interface IndexBuffer {
    void upload(int[] data, int start, int count);
    void dispose();
}
