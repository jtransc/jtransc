package jtransc.game.canvas;

import jtransc.JTranscRender;

public class BaseTexture {
	public final int id;
	public final int width;
	public final int height;

	public BaseTexture(int id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
	}

	public void dispose() {
		JTranscRender.disposeTexture(id);
	}
}
