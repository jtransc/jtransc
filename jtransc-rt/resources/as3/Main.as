package {
	import flash.display.Sprite;
	import flash.system.System;
	import flash.system.fscommand;
	import flash.desktop.NativeApplication;

	public class Main extends flash.display.Sprite {
		public function Main() {
			trace('a');
			//flash.system.System.exit(0);
			flash.desktop.NativeApplication.nativeApplication.exit();

		}
	}
}