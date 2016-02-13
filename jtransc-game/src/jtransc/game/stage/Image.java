package jtransc.game.stage;

import jtransc.game.canvas.Context2D;
import jtransc.game.canvas.Texture;
import jtransc.game.math.Rectangle;

public class Image extends DisplayObject {
    public Texture image;
    public double anchorX = 0;
    public double anchorY = 0;

    public Image(Texture image) {
        this(image, 0, 0);
    }

    public Image(Texture image, double anchorX, double anchorY) {
        this.image = image;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    //(val image: Texture, var anchorX:Double = 0.0, var anchorY:Double = 0.0)
    public void internalRender(Context2D ctx) {
        ctx.drawImage(image, -image.width * anchorX, -image.height * anchorY);
    }

    @Override
    public Rectangle getLocalBounds() {
        return new Rectangle(-image.width * anchorX, -image.height * anchorY, image.width, image.height);
    }
}