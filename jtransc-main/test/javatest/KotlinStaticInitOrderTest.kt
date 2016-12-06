package javatest

object KotlinStaticInitOrderTest {
	@JvmStatic fun main(args: Array<String>) {
		println("KotlinStaticInitOrderTest:")
		println(Easings.EASE_IN_OUT_QUAD(1.0))
	}
}

object Easings {
	val LINEAR: ((ratio: Double) -> Double) = fun(v: Double) = v
	val EASE_IN: ((ratio: Double) -> Double) = fun(v: Double) = v * v * v
	val EASE_OUT: ((ratio: Double) -> Double) = fun(ratio: Double): Double {
		val invRatio = ratio - 1.0;
		return invRatio * invRatio * invRatio + 1;
	}
	val EASE_IN_OUT: ((ratio: Double) -> Double) = easeCombined(EASE_IN, EASE_OUT)
	val EASE_OUT_IN: ((ratio: Double) -> Double) = easeCombined(EASE_OUT, EASE_IN)

	val EASE_IN_BACK: ((ratio: Double) -> Double) = fun(ratio: Double): Double {
		val s = 1.70158
		return Math.pow(ratio, 2.0) * ((s + 1.0) * ratio - s)
	}

	val EASE_OUT_BACK: ((ratio: Double) -> Double) = fun(ratio: Double): Double {
		val invRatio = ratio - 1.0;
		val s = 1.70158;
		return Math.pow(invRatio, 2.0) * ((s + 1.0) * invRatio + s) + 1.0;
	}

	val EASE_IN_OUT_BACK: ((ratio: Double) -> Double) = easeCombined(EASE_IN_BACK, EASE_OUT_BACK)
	val EASE_OUT_IN_BACK: ((ratio: Double) -> Double) = easeCombined(EASE_OUT_BACK, EASE_IN_BACK)
	val EASE_IN_ELASTIC: ((ratio: Double) -> Double) = fun(ratio: Double): Double {
		if (ratio == 0.0 || ratio == 1.0) {
			return ratio;
		} else {
			val p = 0.3;
			val s = p / 4.0;
			val invRatio = ratio - 1;
			return -1.0 * Math.pow(2.0, 10.0 * invRatio) * Math.sin((invRatio - s) * (2.0 * Math.PI) / p);
		}
	}

	val EASE_OUT_ELASTIC: ((ratio: Double) -> Double) = fun(ratio: Double): Double {
		if (ratio == 0.0 || ratio == 1.0) {
			return ratio;
		} else {
			val p = 0.3;
			val s = p / 4.0;
			return Math.pow(2.0, -10.0 * ratio) * Math.sin((ratio - s) * (2.0 * Math.PI) / p) + 1;
		}
	}

	val EASE_IN_OUT_ELASTIC: ((ratio: Double) -> Double) = easeCombined(EASE_IN_ELASTIC, EASE_OUT_ELASTIC)
	val EASE_OUT_IN_ELASTIC: ((ratio: Double) -> Double) = easeCombined(EASE_OUT_ELASTIC, EASE_IN_ELASTIC)

	val EASE_OUT_BOUNCE = fun(ratio: Double): Double {
		val s = 7.5625
		val p = 2.75
		return if (ratio < (1.0 / p)) {
			s * Math.pow(ratio, 2.0);
		} else if (ratio < (2.0 / p)) {
			s * Math.pow(ratio - 1.5 / p, 2.0) + 0.75;
		} else if (ratio < 2.5 / p) {
			s * Math.pow(ratio - 2.25 / p, 2.0) + 0.9375;
		} else {
			s * Math.pow(ratio - 2.625 / p, 2.0) + 0.984375;
		}
	}

	val EASE_IN_BOUNCE: ((ratio: Double) -> Double) = fun(ratio: Double): Double {
		return 1.0 - EASE_OUT_BOUNCE(1.0 - ratio);
	}

	val EASE_IN_OUT_BOUNCE: ((ratio: Double) -> Double) = easeCombined(EASE_IN_BOUNCE, EASE_OUT_BOUNCE)
	val EASE_OUT_IN_BOUNCE: ((ratio: Double) -> Double) = easeCombined(EASE_OUT_BOUNCE, EASE_IN_BOUNCE)

	val EASE_IN_QUAD: ((ratio: Double) -> Double) = fromCubicTBCD(fun(_t: Double, b: Double, c: Double, d: Double): Double {
		val t = _t / d
		return c * t * t + b
	})

	val EASE_OUT_QUAD: ((ratio: Double) -> Double) = fromCubicTBCD(fun(_t: Double, b: Double, c: Double, d: Double): Double {
		val t = _t / d
		return -c * t * (t - 2) + b
	})

	val EASE_IN_OUT_QUAD: ((ratio: Double) -> Double) = fromCubicTBCD(fun(_t: Double, b: Double, c: Double, d: Double): Double {
		val t = _t / (d / 2.0)
		return if (t < 1) {
			(c / 2 * t * t + b)
		} else {
			(-c / 2 * ((t - 1) * ((t - 1) - 2) - 1) + b)
		}
	})

	fun fromCubicTBCD(f: (t: Double, b: Double, c: Double, d: Double) -> Double): ((ratio: Double) -> Double) {
		return fun(ratio: Double): Double {
			return f(ratio, 0.0, 1.0, 1.0)
		}
	}

	fun easeCombined(start: ((ratio: Double) -> Double), end: ((ratio: Double) -> Double)): ((ratio: Double) -> Double) {
		return fun(ratio: Double): Double {
			return if (ratio < 0.5) {
				0.5 * start(ratio * 2.0)
			} else {
				0.5 * end((ratio - 0.5) * 2.0) + 0.5
			}
		}
	}
}
