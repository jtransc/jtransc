package jtransc.game.stage;

import jtransc.game.canvas.Canvas;
import jtransc.game.canvas.Context2D;

public class Stage {
	public Sprite root = new Sprite();

	public void render(Canvas canvas) {
		canvas.start();
		render(canvas.context2D);
		canvas.draw();
	}

	public void render(Context2D ctx) {
		root.render(ctx);
	}

	public void update(int dtMs) {
		root.update(dtMs);
	}
}

