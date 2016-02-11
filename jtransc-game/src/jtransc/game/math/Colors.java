package jtransc.game.math;

public class Colors {
	static public int toOffsetUint(int oR, int oG, int oB, int oA) {
		if (oR == 0 && oG == 0 && oB == 0 && oA == 0) return 0x7F7F7F7F;
		return MathUtils.packUintFast(
			MathUtils.clampf255((oR + 255) / 510.0),
			MathUtils.clampf255((oG + 255) / 510.0),
			MathUtils.clampf255((oB + 255) / 510.0),
			MathUtils.clampf255((oA + 255) / 510.0)
		);
	}

	static public int rgbafToUint(double r, double g, double b, double a, boolean premultiply) {
		if (r >= 1 && g >= 1 && b >= 1) {
			if (a >= 1) {
				return 0xFFFFFFFF;
			} else {
				int av = MathUtils.clampf255(a);
				int ac = (premultiply) ? av : 0xFF;
				return MathUtils.packUintFast(ac, ac, ac, av);
			}
		} else if (premultiply) {
			return MathUtils.pack4fUint(r * a, g * a, b * a, a);
		} else {
			return MathUtils.pack4fUint(r, g, b, a);
		}
	}

	static public int unpackR(int color) {
		return (color >>> 0) & 0xFF;
	}

	static public int unpackG(int color) {
		return (color >>> 8) & 0xFF;
	}

	static public int unpackB(int color) {
		return (color >>> 16) & 0xFF;
	}

	static public int unpackA(int color) {
		return (color >>> 24) & 0xFF;
	}

	static public String unpackRGBAString(int color) {
		return "RGBA(" + Colors.unpackG(color) + "," + Colors.unpackG(color) + "," + Colors.unpackB(color) + "," + Colors.unpackA(color) + ")";
	}
}
