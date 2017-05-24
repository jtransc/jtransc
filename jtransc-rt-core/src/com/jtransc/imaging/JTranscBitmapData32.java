package com.jtransc.imaging;

public class JTranscBitmapData32 {
	public boolean premultiplied;
	public int width;
	public int height;
	public int[] rgba;

	public JTranscBitmapData32(boolean premultiplied, int width, int height, int[] rgba) {
		this.premultiplied = premultiplied;
		this.width = width;
		this.height = height;
		this.rgba = rgba;
	}
}
