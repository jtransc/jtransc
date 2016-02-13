package jtransc.game.stage;

import jtransc.game.JTranscGame;
import jtransc.game.util.Signal;

public class MouseHandler {
    final public Signal<DisplayObject> onHover = new Signal<DisplayObject>();
    final public Signal<DisplayObject> onOut = new Signal<DisplayObject>();
    final public Signal<DisplayObject> onDown = new Signal<DisplayObject>();
    final public Signal<DisplayObject> onUp = new Signal<DisplayObject>();
    final public Signal<DisplayObject> onClick = new Signal<DisplayObject>();

    private boolean lastHover;
    private boolean lastPressed;

    public MouseHandler(final DisplayObject view) {
        final JTranscGame game = JTranscGame.instance;
        view.onUpdate.add(new Signal.Handler<Integer>() {
            @Override
            public void handle(Integer value) {
                boolean hover = view.hitTestGlobal(game.mouse);
                boolean pressed = (game.mouseButtons != 0);
                if (hover) {
                    if (!lastHover) onHover.dispatch(view);

                    if (!lastPressed && pressed) {
                        onDown.dispatch(view);
                    }
                } else {
                    if (lastHover) onOut.dispatch(view);
                }

                if (lastPressed && !pressed) {
                    onUp.dispatch(view);
                    if (hover) {
                        onClick.dispatch(view);
                    }
                }

                lastHover = hover;
                lastPressed = pressed;
            }
        });
    }
}
