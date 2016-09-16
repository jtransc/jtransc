Math.imul = Math.imul || function(a, b) {
	var ah = (a >>> 16) & 0xffff;
	var al = a & 0xffff;
	var bh = (b >>> 16) & 0xffff;
	var bl = b & 0xffff;
	return ((al * bl) + (((ah * bl + al * bh) << 16) >>> 0)|0);
};

Math.clz32 = Math.clz32 || (function (x) { return (x >>>= 0) ? 31 - Math.floor(Math.log(x + 0.5) * Math.LOG2E) : 32; });
Math.fround = Math.fround || (function (array) { return function(x) { return array[0] = x, array[0]; }; })(new Float32Array(1));