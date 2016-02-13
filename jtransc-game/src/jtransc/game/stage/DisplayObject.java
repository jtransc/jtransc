package jtransc.game.stage;

import jtransc.game.canvas.Context2D;
import jtransc.game.event.EventDispatcher;
import jtransc.game.math.Matrix;
import jtransc.game.math.Point;
import jtransc.game.math.MatrixTransform;
import jtransc.game.math.Rectangle;
import jtransc.game.util.Signal;

public class DisplayObject extends EventDispatcher {
    private double x = 0.0;
    private double y = 0.0;
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double rotation = 0.0;
    private double skewX = 0.0;
    private double skewY = 0.0;
    private double speed = 1.0;
    private String name = "";
    Sprite parent = null;
    private Matrix transformationMatrix = new Matrix();
    private Matrix transformationMatrixInverted = new Matrix();
    private boolean dirtyTransformationMatrix = false;
    public Signal<Integer> onUpdate = new Signal<Integer>();
    private MouseHandler mouseHandler;

    public MouseHandler getMouse() {
        if (mouseHandler == null) {
            mouseHandler = new MouseHandler(this);
        }
        return mouseHandler;
    }

    public Sprite getParent() {
        return this.parent;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public Matrix getTransformationMatrix() {
        if (dirtyTransformationMatrix) {
            dirtyTransformationMatrix = false;
            MatrixTransform.apply(transformationMatrix, x, y, scaleX, scaleY, rotation, skewX, skewY);
        }
        return transformationMatrix;
    }

    public Matrix getTransformationMatrixInverted() {
        transformationMatrixInverted.copyFrom(getTransformationMatrix());
        transformationMatrixInverted.invert();
        return transformationMatrixInverted;
    }

    private double alpha = 1.0;

    final public void update(int dtMs) {
        dtMs = (int) (dtMs * speed);
        onUpdate.dispatch(dtMs);
        internalUpdate(dtMs);
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getRotationDegrees() {
        return Math.toDegrees(rotation);
    }

    public void setRotationDegrees(double value) {
        setRotation(Math.toRadians(value));
    }

    public double getX() {
        return x;
    }

    private double _value(double oldValue, double newValue) {
        if (oldValue != newValue) dirtyTransformationMatrix = true;
        return newValue;
    }

    public void setX(double x) {
        this.x = _value(this.x, x);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = _value(this.y, y);
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = _value(this.scaleX, scaleX);
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = _value(this.scaleY, scaleY);
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = _value(this.rotation, rotation);
    }

    public double getSkewX() {
        return skewX;
    }

    public void setSkewX(double skewX) {
        this.skewX = _value(this.skewX, skewX);
    }

    public double getSkewY() {
        return skewY;
    }

    public void setSkewY(double skewY) {
        this.skewY = _value(this.skewY, skewY);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    final public void render(Context2D ctx) {
        double oldAlpha = ctx.getGlobalAlpha();
        ctx.setGlobalAlpha(ctx.getGlobalAlpha() * this.alpha);
        ctx.save();
        ctx.translate(x, y);
        ctx.scale(scaleX, scaleY);
        ctx.rotate(rotation);
        internalRender(ctx);
        ctx.restore();
        ctx.setGlobalAlpha(oldAlpha);
    }

    public void internalRender(Context2D ctx) {
    }

    public void internalUpdate(int dtMs) {
    }

    static private Matrix tempMatrix = new Matrix();

    public Point localToGlobal(Point point) {
        return localToGlobal(point, new Point());
    }

    public Point localToGlobal(Point point, Point target) {
        getConcatTransformationMatrix(getBase(), tempMatrix);
        return tempMatrix.transform(point, target);
    }

    public Point globalToLocal(Point point) {
        return globalToLocal(point, new Point());
    }

    public Point globalToLocal(Point point, Point target) {
        getConcatTransformationMatrix(getBase(), tempMatrix);
        tempMatrix.invert();
        return tempMatrix.transform(point, target);
    }

    private Matrix getConcatTransformationMatrix(DisplayObject targetSpace, Matrix resultMatrix) {
        resultMatrix.identity();
        DisplayObject node = this;
        while (node != null && node != targetSpace) {
            resultMatrix.multiply(resultMatrix, node.getTransformationMatrix());
            node = node.getParent();
        }
        return resultMatrix;
    }

    public DisplayObject getBase() {
        DisplayObject currentObject = this;
        while (currentObject.parent != null) currentObject = currentObject.parent;
        return currentObject;
    }

    //final public Rectangle getBounds() {
    //    return getLocalBounds();
    //}

    public Rectangle getLocalBounds() {
        return new Rectangle();
    }

    public boolean hitTestGlobal(Point point) {
        return getLocalBounds().contains(this.globalToLocal(point));
    }
}

