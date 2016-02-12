package jtransc.game.math;

public class MatrixTransform {
    public double x = 0;
    public double y = 0;
    public double scaleX = 1;
    public double scaleY = 1;
    public double rotation = 0;
    public double skewX = 0;
    public double skewY = 0;

    public Matrix applyMatrix(Matrix matrix) {
        return matrix.setTransform(x, y, scaleX, scaleY, rotation, skewX, skewY);
    }

    public MatrixTransform setMatrix(Matrix matrix) {
        double PI_4 = Math.PI / 4.0;
        this.x = matrix.tx;
        this.y = matrix.ty;

        this.skewX = Math.atan(-matrix.c / matrix.d);
        this.skewY = Math.atan(matrix.b / matrix.a);

        // Faster isNaN
        if (this.skewX != this.skewX) this.skewX = 0.0;
        if (this.skewY != this.skewY) this.skewY = 0.0;

        this.scaleY = (this.skewX > -PI_4 && this.skewX < PI_4) ? (matrix.d / Math.cos(this.skewX)) : (-matrix.c / Math.sin(this.skewX));
        this.scaleX = (this.skewY > -PI_4 && this.skewY < PI_4) ? (matrix.a / Math.cos(this.skewY)) : (matrix.b / Math.sin(this.skewY));

        if (Math.abs(this.skewX - this.skewY) < 0.0001) {
            this.rotation = this.skewX;
            this.skewX = 0.0;
            this.skewY = 0.0;
        } else {
            this.rotation = 0.0;
        }

        //updateTopology();

        return this;
    }

    static public Matrix apply(Matrix matrix, double x, double y, double scaleX, double scaleY, double rotation, double skewX, double skewY) {
        return matrix.setTransform(x, y, scaleX, scaleY, rotation, skewX, skewY);
    }
}
