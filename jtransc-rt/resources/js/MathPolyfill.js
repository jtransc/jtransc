Math.imul = Math.imul || function(a, b) {
	'use strict';
	var ah = (a >>> 16) & 0xffff;
	var al = a & 0xffff;
	var bh = (b >>> 16) & 0xffff;
	var bl = b & 0xffff;
	// the shift by 0 fixes the sign on the high part
	// the final |0 converts the unsigned value into a signed value
	return ((al * bl) + (((ah * bl + al * bh) << 16) >>> 0)|0);
};

Math.clz32 = Math.clz32 || (function () {
	'use strict';

	var table = [
		32, 31,  0, 16,  0, 30,  3,  0, 15,  0,  0,  0, 29, 10,  2,  0,
		0,  0, 12, 14, 21,  0, 19,  0,  0, 28,  0, 25,  0,  9,  1,  0,
		17,  0,  4,   ,  0,  0, 11,  0, 13, 22, 20,  0, 26,  0,  0, 18,
		5,  0,  0, 23,  0, 27,  0,  6,  0, 24,  7,  0,  8,  0,  0,  0
	];

	// Adapted from an algorithm in Hacker's Delight, page 103.
	return function (x) {
		// Note that the variables may not necessarily be the same.

		// 1. Let n = ToUint32(x).
		var v = Number(x) >>> 0

		// 2. Let p be the number of leading zero bits in the 32-bit binary representation of n.
		v |= v >>> 1
		v |= v >>> 2
		v |= v >>> 4
		v |= v >>> 8
		v |= v >>> 16
		v = table[Math.imul(v, 0x06EB14F9) >>> 26]

		// Return p.
		return v
	}
})();

Math.fround = Math.fround || (function (array) {
	'use strict';
	return function(x) {
		return array[0] = x, array[0];
	};
})(new Float32Array(1));