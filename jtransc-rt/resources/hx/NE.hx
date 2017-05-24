// https://haxe.io/roundups/wwx/c++-magic/
#if cpp
@:include('N.h')
#end
extern class NE {
	#if cpp
	@:native("N_obj::_i2b") static public function i2b(v:Int):Int;
	@:native("N_obj::_i2s") static public function i2s(v:Int):Int;
	@:native("N_obj::_i2c") static public function i2c(v:Int):Int;
	#end
}