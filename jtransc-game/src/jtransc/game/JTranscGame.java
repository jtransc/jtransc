package jtransc.game;

import jtransc.JTranscEndian;
import jtransc.JTranscEventLoop;
import jtransc.JTranscSystem;
import jtransc.JTranscVersion;
import jtransc.game.canvas.Canvas;
import jtransc.game.canvas.Texture;
import jtransc.game.stage.Sprite;
import jtransc.game.stage.Stage;

public class JTranscGame {
	public final Canvas canvas;
	public final Stage stage;
	public final Sprite root;

	private int lastTime = -1;

	public JTranscGame(Canvas canvas, Stage stage) {
		this.canvas = canvas;
		this.stage = stage;
		this.root = stage.root;
	}

	public Texture image(String path, int width, int height) {
		return canvas.image(path, width, height);
	}

	public interface Handler {
		void init(JTranscGame game);
	}

	static public void init(final Handler entry) {
		final Canvas canvas = new Canvas();
		JTranscEventLoop.init(new Runnable() {
			@Override
			public void run() {
				System.out.println("Init");
				System.out.println("JTransc version:" + JTranscVersion.getVersion());
				System.out.println("Endian isLittleEndian:" + JTranscEndian.isLittleEndian());
				System.out.println("Endian isBigEndian:" + JTranscEndian.isBigEndian());

				final Stage stage = new Stage();
				final JTranscGame game = new JTranscGame(canvas, stage);

				entry.init(game);

				JTranscEventLoop.loop(new Runnable() {
					@Override
					public void run() {
						int currentTime = JTranscSystem.stamp();
						if (game.lastTime < 0) game.lastTime = currentTime;
						int elapsed = currentTime - game.lastTime;
						stage.update(elapsed);
						game.lastTime = currentTime;
					}
				}, new Runnable() {
					@Override
					public void run() {
						stage.render(canvas);
					}
				});
			}
		});
	}
}
