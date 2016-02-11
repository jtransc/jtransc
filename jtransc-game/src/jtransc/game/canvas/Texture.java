package jtransc.game.canvas;

import jtransc.game.math.MathUtils;

public class Texture {
	public final BaseTexture base;
	public final int top;
	public final int left;
	public final int right;
	public final int bottom;
	public final int width;
	public final int height;
	public final float tx0, ty0, tx1, ty1;

	private Texture(
		BaseTexture base,
		int left, int top,
		int right, int bottom
	) {
		this.base = base;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.width = right - left;
		this.height = bottom - top;
		this.tx0 = (float) (((double) left) / (double) base.width);
		this.ty0 = (float) (((double) top) / (double) base.height);
		this.tx1 = (float) (((double) right) / (double) base.width);
		this.ty1 = (float) (((double) bottom) / (double) base.height);
	}

	public Texture(BaseTexture base) {
		this(base, 0, 0, base.width, base.height);
	}

	public Texture slice(int x, int y, int width, int height) {
		int l2 = MathUtils.clamp(left + x, left, right);
		int r2 = MathUtils.clamp(left + x + width, left, right);
		int t2 = MathUtils.clamp(top + y, top, bottom);
		int b2 = MathUtils.clamp(top + y + height, top, bottom);
		return new Texture(base, l2, t2, r2, b2);
	}
}
