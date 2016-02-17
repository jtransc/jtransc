package jtransc.game;

import jtransc.*;
import jtransc.game.audio.Sound;
import jtransc.game.canvas.Canvas;
import jtransc.game.canvas.Texture;
import jtransc.game.event.Event;
import jtransc.game.event.KeyEvent;
import jtransc.game.event.MouseEvent;
import jtransc.game.math.Point;
import jtransc.game.stage.Sprite;
import jtransc.game.stage.Stage;
import jtransc.game.ui.Keys;

public class JTranscGame {
	public final Canvas canvas;
	public final Stage stage;
	public final Sprite root;

	private int lastTime = -1;
    final public Point mouse = new Point(-1000, -1000);
    public int mouseButtons = 0;

    private boolean[] pressingKeys = new boolean[Keys.MAX];

    static public JTranscGame instance;

	public JTranscGame(Canvas canvas, Stage stage) {
		this.canvas = canvas;
		this.stage = stage;
		this.root = stage.root;
        JTranscGame.instance = this;
	}

	public Sound sound(String path) {
		return new Sound(path);
	}

	public Texture image(String path, int width, int height) {
		return canvas.image(path, width, height);
	}

    public boolean isPressing(int keyCode) {
        return pressingKeys[keyCode];
    }

    public interface Handler {
		void init(JTranscGame game);
	}

	static public void init(final Handler entry) {
		final Canvas canvas = new Canvas();
		JTranscEventLoop.init(new Runnable() {
			@Override
			public void run() {
				System.out.println("JTranscGame.Init");
				System.out.println("JTransc version:" + JTranscVersion.getVersion());
				System.out.println("Endian isLittleEndian:" + JTranscEndian.isLittleEndian());
				System.out.println("Endian isBigEndian:" + JTranscEndian.isBigEndian());

				final Stage stage = new Stage();
				final JTranscGame game = new JTranscGame(canvas, stage);

				entry.init(game);
                game.registerEvents();

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

    private void registerEvents() {
        final JTranscGame game = this;

        final KeyEvent keyEvent = new KeyEvent();

        JTranscInput.addHandler(new JTranscInput.Handler() {
            @Override
            public void onKeyTyped(JTranscInput.KeyInfo info) {
            }

            @Override
            public void onKeyDown(JTranscInput.KeyInfo info) {
                keyEvent.type = KeyEvent.Type.DOWN;
                keyEvent.keyCode = info.keyCode;
                stage.root.dispatchEvent(keyEvent);
                game.pressingKeys[info.keyCode] = true;
            }

            @Override
            public void onKeyUp(JTranscInput.KeyInfo info) {
                keyEvent.type = KeyEvent.Type.UP;
                keyEvent.keyCode = info.keyCode;
                stage.root.dispatchEvent(keyEvent);
                game.pressingKeys[info.keyCode] = false;
            }

            @Override
            public void onGamepadPressed(JTranscInput.GamepadInfo info) {
            }

            @Override
            public void onGamepadRelepased(JTranscInput.GamepadInfo info) {
            }

            @Override
            public void onMouseDown(JTranscInput.MouseInfo info) {
                game.setMouseInfo(info);
            }

            @Override
            public void onMouseUp(JTranscInput.MouseInfo info) {
                game.setMouseInfo(info);
            }

            @Override
            public void onMouseMove(JTranscInput.MouseInfo info) {
                game.setMouseInfo(info);
            }

            @Override
            public void onMouseScroll(JTranscInput.MouseInfo info) {
                game.setMouseInfo(info);
            }

            @Override
            public void onTouchDown(JTranscInput.TouchInfo info) {
            }

            @Override
            public void onTouchDrag(JTranscInput.TouchInfo info) {
            }

            @Override
            public void onTouchUp(JTranscInput.TouchInfo info) {
            }
        });
    }

    private final MouseEvent mouseEvent = new MouseEvent();

    private void setMouseInfo(JTranscInput.MouseInfo info) {
        mouseEvent.position.x = info.x;
        mouseEvent.position.y = info.y;
        mouseEvent.buttons = info.buttons;
        mouse.setTo(info.x, info.y);
        mouseButtons = info.buttons;
        stage.root.dispatchEvent(mouseEvent);
        //System.out.println("Event: " + getMouse);
    }
}
