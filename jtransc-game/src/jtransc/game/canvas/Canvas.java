package jtransc.game.canvas;

import jtransc.JTranscRender;
import jtransc.game.batch.BatchBuilder;

public class Canvas {
	BatchBuilder batches = new BatchBuilder();
	public Context2D context2D = new Context2D(this);
	public int width = 640;
	public int height = 480;

	public Texture image(String path, int width, int height) {
		return new Texture(new BaseTexture(JTranscRender.createTexture(path, width, height), width, height));
	}

	public void start() {
		batches.start();
	}

	public void draw() {
		batches.render();
		batches.reset();
		context2D.reset();
	}
}
