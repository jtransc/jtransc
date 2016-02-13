package jtransc.game.stage;

import jtransc.game.canvas.Texture;

public class TileSet {
	public Texture[] tiles;
	public int width;
	public int height;

	public TileSet(Texture[] tiles, int width, int height) {
		this.width = width;
		this.height = height;
		this.tiles = tiles;
	}
}
