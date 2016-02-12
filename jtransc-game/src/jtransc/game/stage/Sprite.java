package jtransc.game.stage;

import jtransc.game.canvas.Context2D;
import jtransc.game.event.Event;

import java.util.ArrayList;

public class Sprite extends DisplayObject {
    ArrayList<DisplayObject> children = new ArrayList<DisplayObject>();

    public DisplayObject addChild(DisplayObject child) {
        children.add(child);
        return child;
    }

    public void removeChild(DisplayObject child) {
        children.remove(child);
    }

    public void internalRender(Context2D ctx) {
        for (int n = 0; n < children.size(); n++) {
            children.get(n).render(ctx);
        }
    }

    @Override
    public void internalUpdate(int dtMs) {
        for (int n = 0; n < children.size(); n++) {
            children.get(n).update(dtMs);
        }
    }

    @Override
    public void dispatchEvent(Event event) {
        super.dispatchEvent(event);
        for (int n = 0; n < children.size(); n++) {
            children.get(n).dispatchEvent(event);
        }
    }
}

