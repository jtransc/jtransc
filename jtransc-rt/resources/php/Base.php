<?php

// JTransc {{ JTRANSC_VERSION }} : https://github.com/jtransc/jtransc

class N {
	static function init() {
	}

	static public function irem($a, $b) { return $a % $b; }
	static public function idiv($a, $b) { return floor($a / $b); } // intdiv

	static public function z2i($v) { return $v ? 1 : 0; }
	static public function i2c($v) { return $v & 0xFFFF; }

	static function lnew($h, $l) {
		throw new Exception("Not implemented");
	}

	static function strLitEscape($str) {
		return N::str($str);
	}

	// @TODO: Use native strings
	// @TODO: Unicode (use utf-8?)
	static function str($str) {
		$len = strlen($str);
		$chars = new JA_C($len);
		for ($n = 0; $n < $len; $n++) $chars->set($n, ord(substr($str, $n, 1)));
		return {% CONSTRUCTOR java.lang.String:([C)V %}($chars);
	}

	static function istr($str) {
		$out = '';
		$len = $str->{% METHOD java.lang.String:length %}();
		for ($n = 0; $n < $len; $n++) $out .= chr($str->{% METHOD java.lang.String:charAt %}($n));
		return $out;
	}

	static public function strArray($array) {
		return JA_L::create(array_map(function($v) { return N::str($v); }, $array), 'Ljava/lang/String;');
	}
}

class JA_0 extends {% CLASS java.lang.Object %} {
	public $length = 0;
	public $desc = '';
	public $data = null;

	public function __construct($length, $desc, $default = 0) {
		//echo "Allocating $desc: $length with default value '$default'\n";
		//debug_print_backtrace();
		$this->length = $length;
		$this->desc = $desc;
		$this->data = array_fill(0, $length, $default);
	}

	public function set($index, $value) { $this->data[$index] = $value; }
	public function get($index) { return $this->data[$index]; }
}

class JA_B extends JA_0 {
	public function __construct($length, $desc = '[B') { parent::__construct($length, $desc, 0); }
}

class JA_Z extends JA_B {
	public function __construct($length) { parent::__construct($length, '[Z'); }
}

class JA_C extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[C', 0); }
}

class JA_S extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[S', 0); }
}

class JA_I extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[I', 0); }
}

class JA_J extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[J', 0); }
}

class JA_F extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[F', 0.0); }
}

class JA_D extends JA_0 {
	public function __construct($length) { parent::__construct($length, '[D', 0.0); }
}

class JA_L extends JA_0 {
	public function __construct($length, $desc) { parent::__construct($length, $desc, null); }

	static function create($items, $desc) {
		$count = sizeof($items);
		$out = new JA_L($count, $desc);
		for ($n = 0; $n < $count; $n++) $out->set($n, $items[$n]);
		return $out;
	}
}

class JavaWrappedException extends Exception {
}

/* ## BODY ## */
