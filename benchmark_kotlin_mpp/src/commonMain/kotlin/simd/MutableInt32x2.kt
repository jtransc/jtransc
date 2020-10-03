package simd

class MutableInt32x2  private constructor(
	var x: Int, var y: Int
) {
	operator fun set(x: Int, y: Int) {
		this.x = x
		this.y = y
	}

	fun setToAdd(l: MutableInt32x2, r: MutableInt32x2) {
		x = l.x + r.x
		y = l.y + r.y
	}

	companion object {
		fun create(): MutableInt32x2 {
			return create(0, 0)
		}

		fun create(x: Int, y: Int): MutableInt32x2 {
			return MutableInt32x2(x, y)
		}
	}
}