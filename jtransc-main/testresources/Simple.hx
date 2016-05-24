class Simple {
	public function new() {
	}

	public function flush() {
		trace('flush');
	}
}

typedef DynamicIntMap = haxe.ds.IntMap<Dynamic>;