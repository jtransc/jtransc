package jtransc.game.math;

public class IPoint {
    public int x;
    public int y;

    public IPoint() {
        this(0, 0);
    }

    public IPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public IPoint setTo(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public void copyFrom(IPoint that) {
        this.x = that.x;
        this.y = that.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPoint that = (IPoint) o;
        return this.x == that.x && this.y == that.y;

    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "IPoint(" + x + ", " + y + ")";
    }
}
