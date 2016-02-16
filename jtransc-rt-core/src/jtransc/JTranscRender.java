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

public class JTranscRender {
    static public Impl impl = new Impl() {
        @Override
        public int createTexture(String path, int width, int height) {
            System.out.println("JTranscRender.createTexture:" + path + ", size:" + width + "x" + height);
            return -1;
        }

        @Override
        public int createTextureMemory(int[] data, int width, int height, int format) {
            System.out.println("JTranscRender.createTextureMemory:" + data.length + ", size:" + width + "x" + height + ", " + format);
            return -1;
        }

        @Override
        public int createTextureEncoded(byte[] data, int width, int height) {
            System.out.println("JTranscRender.createTextureEncoded:" + data.length + ", size:" + width + "x" + height + "");
            return -1;
        }

        @Override
        public void disposeTexture(int textureId) {
            System.out.println("JTranscRender.disposeTexture:" + textureId);
        }

        @Override
        public void render(FastMemory vertices, int vertexCount, short[] indices, int indexCount, int[] batches, int batchCount) {
            System.out.println("JTranscRender.render: vertices=" + vertexCount + ", indices=" + indexCount + " batches=" + batchCount);
        }
    };

    static public int createTexture(String path, int width, int height) {
        return impl.createTexture(path, width, height);
    }

    static public int createTextureMemory(int[] data, int width, int height, int type) {
        return impl.createTextureMemory(data, width, height, type);
    }

    static public int createTextureEncoded(byte[] data, int width, int height) {
        return impl.createTextureEncoded(data, width, height);
    }

    static public void disposeTexture(int textureId) {
        impl.disposeTexture(textureId);
    }

    static public final int BLEND_INVALID = -1;
    //static public final int BLEND_MAX = 16;
    static public final int BLEND_NORMAL = 1;
    static public final int BLEND_ADD = 8;

    static public final int MASK_NONE = 0;
    static public final int MASK_SHAPE = 1;
    static public final int MASK_CONTENT = 2;

    static public final int TYPE_RGBA = 0;

    /**
     * - Vertices is a float[] array containing 6 word elements:
     * 0. x(float)
     * 1. y(float)
     * 2. tx(float)
     * 3. ty(float)
     * 4. col1(int/rgba8) multiplicative tinting
     * 5. col2(int/rgba8) additive tinting
     * - Indices contains indices to upload to the gpu:
     * 0. index (short)
     * - Batches contains a 16 word elements using the following:
     * 0. indexStart(int)
     * 1. triangleCount(int)
     * 2. texture(int) (being -1 a solid color)
     * 3. blendMode(int) BLEND_NORMAL=1, BLEND_ADD=8
     * 4. maskType(int) MASK_NONE=0, MASK_SHAPE=1, MASK_CONTENT=2
     * 5. stencilIndex(int)
     * 6. scissorLeft(int)
     * 7. scissorTop(int)
     * 8. scissorRight(int)
     * 9. scissorBottom(int)
     * 10. _reserved_
     * 11. _reserved_
     * 12. _reserved_
     * 13. _reserved_
     * 14. _reserved_
     * 15. _reserved_
     */
    static public void render(FastMemory vertices, int vertexCount, short[] indices, int indexCount, int[] batches, int batchCount) {
        impl.render(vertices, vertexCount, indices, indexCount, batches, batchCount);
    }

    public interface Impl {
        int createTexture(String path, int width, int height);

        int createTextureMemory(int[] data, int width, int height, int format);

        int createTextureEncoded(byte[] data, int width, int height);

        void disposeTexture(int textureId);

        void render(FastMemory vertices, int vertexCount, short[] indices, int indexCount, int[] batches, int batchCount);
    }
}
