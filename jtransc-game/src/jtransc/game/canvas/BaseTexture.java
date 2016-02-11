package jtransc.game.canvas;

import jtransc.JTranscRender;

public class BaseTexture {
	public final int id;
	public final int width;
	public final int height;

	public BaseTexture(int id, int height, int width) {
		this.id = id;
		this.height = height;
		this.width = width;
	}

	public void dispose() {
		JTranscRender.disposeTexture(id);
	}
}
