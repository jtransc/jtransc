package jtransc.game.event;

public class KeyEvent extends Event {
    public enum Type { UP, DOWN; }

    public Type type;
    public int keyCode;
}
