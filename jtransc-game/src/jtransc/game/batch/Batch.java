package jtransc.game.batch;

import jtransc.JTranscRender;
import jtransc.game.math.Rectangle;

public class Batch {
	public int startIndex = 0;
	public int triangleCount = 0; // triangleCount
	public int texture = 0; // texture(int)
	public int blendMode = JTranscRender.BLEND_NORMAL; // blendMode(int) BLEND_NORMAL=1, BLEND_ADD=8
	public int maskType = 0;
	public int stencilIndex = 0;
	public Rectangle scissors = new Rectangle();

	public void reset() {
		startIndex = 0;
		triangleCount = 0;
		texture = 0;
		blendMode = 1;
		maskType = 0;
		stencilIndex = 0;
		scissors.setToBounds(0, 0, 0, 0);
	}

	public void write(int[] batches, int offset) {
		batches[offset + 0] = startIndex;
		batches[offset + 1] = triangleCount;
		batches[offset + 2] = texture;
		batches[offset + 3] = blendMode;
		batches[offset + 4] = maskType;
		batches[offset + 5] = stencilIndex;
		batches[offset + 6] = (int)scissors.getLeft();
		batches[offset + 7] = (int)scissors.getTop();
		batches[offset + 8] = (int)scissors.getRight();
		batches[offset + 9] = (int)scissors.getBottom();
		batches[offset + 10] = 0;
		batches[offset + 11] = 0;
		batches[offset + 12] = 0;
		batches[offset + 13] = 0;
		batches[offset + 14] = 0;
		batches[offset + 15] = 0;
	}
}