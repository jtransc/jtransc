package jtransc.game.stage;

import jtransc.game.canvas.Context2D;
import jtransc.game.canvas.Texture;
import jtransc.game.event.Event;
import jtransc.game.math.Matrix;
import jtransc.game.math.Point;

import java.util.ArrayList;
import java.util.Objects;

public class Sprite extends DisplayObject {
    ArrayList<DisplayObject> children = new ArrayList<DisplayObject>();

    public <T extends  DisplayObject> T addChild(T child) {
        Sprite parent = child.getParent();
        if (parent != null) parent.removeChild(child);
        child.parent = this;
        children.add(child);
        return child;
    }

    // Factory
    public Image image(Texture texture) {
        return addChild(new Image(texture));
    }
    public Sprite sprite() {
        return addChild(new Sprite());
    }

    public DisplayObject get(String name) {
        for (DisplayObject child : children) {
            if (Objects.equals(child.getName(), name)) return child;
        }
        return null;
    }

    public void removeChild(DisplayObject child) {
        if (child.getParent() == this) {
            children.remove(child);
            child.parent = null;
        }
    }

    public void removeChildren() {
        for (DisplayObject child : children) {
            child.parent = null;
        }

        children.clear();
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

    @Override
    public boolean hitTestGlobal(Point point) {
        for (DisplayObject child : children) {
            if (child.hitTestGlobal(point)) return true;
        }
        return false;
    }
}

