package jtransc.game.event;

import jtransc.game.math.Point;

public class MouseEvent extends Event {
    public Point position = new Point();
    public int buttons = 0;
}
