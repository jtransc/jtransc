package jtransc.game.stage;

import jtransc.game.canvas.Context2D;
import jtransc.game.util.IntArray2;

public class TileMap extends DisplayObject {
	public final IntArray2 map;
	public TileSet tileset;

	public TileMap(int height, int width, TileSet tileset) {
		this.map = new IntArray2(width, height);
		this.tileset = tileset;
	}

	public int getWidth() {
		return this.map.width;
	}

	public int getHeight() {
		return this.map.height;
	}

	public int set(int x, int y, int tile) {
		this.map.set(x, y, tile);
		return tile;
	}

	public int get(int x, int y) {
		return this.map.get(x, y);
	}

	@Override
	public void internalRender(Context2D ctx) {
		final int width = tileset.width;
		final int height = tileset.height;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				ctx.drawImage(tileset.tiles[get(x, y)], x * width, y * height);
			}
		}
	}
}
