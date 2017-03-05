package {
	import flash.display.Sprite;
	import flash.system.System;
	import flash.system.fscommand;
	import flash.desktop.NativeApplication;
	import flash.utils.setTimeout;
	{% for c in BASE_CLASSES_FQNAMES %}
	import {{ c }};
	{% end %}

	public class Main extends flash.display.Sprite {
		public function Main() {
			flash.utils.setTimeout(function(): void { Main.init(); }, 0);
		}

		static private function init(): void {
			{% for sc in STATIC_CONSTRUCTORS %}
			{{ sc }}
			{% end %}
			trace('a');
			//flash.system.System.exit(0);
			flash.desktop.NativeApplication.nativeApplication.exit();
		}
	}
}