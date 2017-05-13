package {

import flash.display.Sprite;
import flash.system.System;
import flash.system.fscommand;
import flash.desktop.NativeApplication;
import flash.utils.setTimeout;
import Bootstrap;
{% for c in BASE_CLASSES_FQNAMES %}
import {{ c }};
{% end %}

public class Main extends flash.display.Sprite {
	public function Main() {
		flash.utils.setTimeout(function(): void { Main.doMain(); }, 0);
	}

	static private function staticInit(): void {
		Bootstrap.init();
		{% for sc in STATIC_CONSTRUCTORS %}{{ sc }}
		{% end %}
	}

	static private function doActualMain(): void {
		{{ MAIN_METHOD_CALL }}
	}

	static private function doMain(): void {
		var air: Boolean = inAir();

		if (air) {
			try {
				staticInit();
				doActualMain();
			} catch (e: *) {
				if (e is Error) {
					trace((e as Error).getStackTrace())
				} else {
					trace(e);
				}
			}
			flash.desktop.NativeApplication.nativeApplication.exit();
		} else {
			staticInit();
			doActualMain();
		}
	}

	static private function inAir(): Boolean {
		try {
			flash.utils.getDefinitionByName('flash.desktop.NativeApplication');
			return true;
		} catch (e: *) {
			return false;
		}
	}
}

}