package jtransc.game.math;

public class ColorTransform {
	private double redMultiplier;
	private double greenMultiplier;
	private double blueMultiplier;
	private double alphaMultiplier;
	private int redOffset;
	private int greenOffset;
	private int blueOffset;
	private int alphaOffset;

	public double getRedMultiplier() {
		return redMultiplier;
	}

	public void setRedMultiplier(double redMultiplier) {
		this.redMultiplier = redMultiplier;
	}

	public double getGreenMultiplier() {
		return greenMultiplier;
	}

	public void setGreenMultiplier(double greenMultiplier) {
		this.greenMultiplier = greenMultiplier;
	}

	public double getBlueMultiplier() {
		return blueMultiplier;
	}

	public void setBlueMultiplier(double blueMultiplier) {
		this.blueMultiplier = blueMultiplier;
	}

	public double getAlphaMultiplier() {
		return alphaMultiplier;
	}

	public void setAlphaMultiplier(double alphaMultiplier) {
		this.alphaMultiplier = alphaMultiplier;
	}

	public int getRedOffset() {
		return redOffset;
	}

	public void setRedOffset(int redOffset) {
		this.redOffset = redOffset;
	}

	public int getGreenOffset() {
		return greenOffset;
	}

	public void setGreenOffset(int greenOffset) {
		this.greenOffset = greenOffset;
	}

	public int getBlueOffset() {
		return blueOffset;
	}

	public void setBlueOffset(int blueOffset) {
		this.blueOffset = blueOffset;
	}

	public int getAlphaOffset() {
		return alphaOffset;
	}

	public void setAlphaOffset(int alphaOffset) {
		this.alphaOffset = alphaOffset;
	}

	public ColorTransform() {
		this(1, 1, 1, 1, 0, 0, 0, 0);
	}

	public ColorTransform(double redMultiplier, double greenMultiplier, double blueMultiplier, double alphaMultiplier) {
		this(redMultiplier, greenMultiplier, blueMultiplier, alphaMultiplier, 0, 0, 0, 0);
	}

	public ColorTransform(double redMultiplier, double greenMultiplier, double blueMultiplier, double alphaMultiplier, int redOffset, int greenOffset, int blueOffset, int alphaOffset) {
		this.redMultiplier = redMultiplier;
		this.greenMultiplier = greenMultiplier;
		this.blueMultiplier = blueMultiplier;
		this.alphaMultiplier = alphaMultiplier;
		this.redOffset = redOffset;
		this.greenOffset = greenOffset;
		this.blueOffset = blueOffset;
		this.alphaOffset = alphaOffset;
	}

	public ColorTransform setTo(double redMultiplier, double greenMultiplier, double blueMultiplier, double alphaMultiplier, int redOffset, int greenOffset, int blueOffset, int alphaOffset) {
		this.redMultiplier = redMultiplier;
		this.greenMultiplier = greenMultiplier;
		this.blueMultiplier = blueMultiplier;
		this.alphaMultiplier = alphaMultiplier;
		this.redOffset = redOffset;
		this.greenOffset = greenOffset;
		this.blueOffset = blueOffset;
		this.alphaOffset = alphaOffset;
		return this;
	}

	public int getMultiplierInt(boolean premultiply) {
		return Colors.rgbafToUint(redMultiplier, greenMultiplier, blueMultiplier, alphaMultiplier, premultiply);
	}

	public int getOffsetInt() {
		return Colors.toOffsetUint(redOffset, greenOffset, blueOffset, alphaOffset);
	}


	public void copyFrom(ColorTransform that) {
		this.setTo(
			that.redMultiplier, that.greenMultiplier, that.blueMultiplier, that.alphaMultiplier,
			that.redOffset, that.greenOffset, that.blueOffset, that.alphaOffset
		);
	}
}
