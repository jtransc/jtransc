package jtransc.game.batch;

import jtransc.*;
import jtransc.game.math.Point;

public class BatchBuilder {
	public FastMemory vertices = new FastMemory(4 * 6 * 16 * 1024);
	public FastMemory4Float verticesFloat = new FastMemory4Float(vertices);
	public FastMemory4Int verticesInt = new FastMemory4Int(vertices);
	public short[] indices = new short[6 * 1024 * 6];
	public int[] batches = new int[16 * 256];

	public int verticesIndex = 0;
	public int indicesIndex = 0;
	public int batchCount = 0;

	// Data
	public Batch current = new Batch();

	public void start() {
		Mem.select(vertices);
	}

	public void reset() {
		verticesIndex = 0;
		indicesIndex = 0;
		batchCount = 0;
		current.reset();
	}

	private void flush() {
		if (current.triangleCount <= 0) return;
		current.write(batches, batchCount * 16);
		batchCount++;
		current.startIndex = indicesIndex;
		current.triangleCount = 0;
	}

	public void quad(int texture, Point p0, Point p1, Point p2, Point p3, float tx0, float ty0, float tx1, float ty1, int color1, int color2) {
		if (current.texture != texture) {
			flush();
			current.texture = texture;
		}
		int vii = verticesIndex;
		int vi = vii * 6;
		short[] i = indices;
		int ii = indicesIndex;

		//println("${p0.x}, ${p0.y}, ${p1.x}, ${p1.y} :: $x, $y, $w, $h :: $matrix")
		Mem.sf32(vi + 0, (float) p0.x);
		Mem.sf32(vi + 1, (float) p0.y);
		Mem.sf32(vi + 2, tx0);
		Mem.sf32(vi + 3, ty0);
		Mem.si32(vi + 4, color1);
		Mem.si32(vi + 5, color2);

		Mem.sf32(vi + 6, (float) p1.x);
		Mem.sf32(vi + 7, (float) p1.y);
		Mem.sf32(vi + 8, tx1);
		Mem.sf32(vi + 9, ty0);
		Mem.si32(vi + 10, color1);
		Mem.si32(vi + 11, color2);

		Mem.sf32(vi + 12, (float) p2.x);
		Mem.sf32(vi + 13, (float) p2.y);
		Mem.sf32(vi + 14, tx0);
		Mem.sf32(vi + 15, ty1);
		Mem.si32(vi + 16, color1);
		Mem.si32(vi + 17, color2);

		Mem.sf32(vi + 18, (float) p3.x);
		Mem.sf32(vi + 19, (float) p3.y);
		Mem.sf32(vi + 20, tx1);
		Mem.sf32(vi + 21, ty1);
		Mem.si32(vi + 22, color1);
		Mem.si32(vi + 23, color2);

		i[ii + 0] = (short) (vii + 0);
		i[ii + 1] = (short) (vii + 1);
		i[ii + 2] = (short) (vii + 2);
		i[ii + 3] = (short) (vii + 1);
		i[ii + 4] = (short) (vii + 3);
		i[ii + 5] = (short) (vii + 2);

		verticesIndex += 4;
		indicesIndex += 6;
		current.triangleCount += 2;
	}

	public void render() {
		flush();
		//println(vertices.slice(0 until 4 * 6))
		//println(indices.slice(0 until 6))
		//println(batches[0])
		//println(batches[1])
		//println(batches[2])
		//println(batches[3])
		JTranscRender.render(vertices, verticesIndex, indices, indicesIndex, batches, batchCount);
	}
}

