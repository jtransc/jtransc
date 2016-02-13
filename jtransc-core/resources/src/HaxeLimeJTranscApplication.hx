class HaxeLimeJTranscApplication extends lime.app.Application {
	public override function onPreloadComplete():Void {
		//switch (renderer.context) {
		//	case FLASH(sprite): #if flash initializeFlash(sprite); #end
		//	case OPENGL (gl):
		//	default:
		//	throw "Unsupported render context";
		//}
	}

	private var initialized = false;
	private var initializedRenderer = false;
	public override function render(renderer:lime.graphics.Renderer) {
		super.render(renderer);
		if (!initializedRenderer) {
			initializedRenderer = true;
			HaxeLimeRender.setRenderer(renderer);
		}
		HaxeLimeRender.setSize(window.width, window.height);
		if (HaxeLimeRender.isInitialized()) {
			if (!initialized && HaxeNatives.initHandler != null) {
				initialized = true;
				HaxeNatives.initHandler();
			}
			if (HaxeNatives.renderHandler != null) HaxeNatives.renderHandler();
		}
	}

	public override function update(deltaTime:Int) {
		super.update(deltaTime);
		if (HaxeLimeRender.isInitialized()) {
			if (HaxeNatives.updateHandler != null) HaxeNatives.updateHandler();
		}
	}

	public function new() {
		super();
		HaxeNatives.enabledDefaultEventLoop = false;
		addModule(new JTranscModule());
	}
}

class JTranscModule extends lime.app.Module {
	override public function onMouseUp (window:lime.ui.Window, x:Float, y:Float, button:Int):Void {
		jtransc.JTranscInput_.mouseInfo.x = Std.int(x);
		jtransc.JTranscInput_.mouseInfo.y = Std.int(y);
		jtransc.JTranscInput_.mouseInfo.buttons &= ~(1 << button);
		jtransc.JTranscInput_.impl.onMouseUp_Ljtransc_JTranscInput_MouseInfo__V(jtransc.JTranscInput_.mouseInfo);
	}
	override public function onMouseDown (window:lime.ui.Window, x:Float, y:Float, button:Int):Void {
		jtransc.JTranscInput_.mouseInfo.x = Std.int(x);
		jtransc.JTranscInput_.mouseInfo.y = Std.int(y);
		jtransc.JTranscInput_.mouseInfo.buttons |= 1 << button;
		jtransc.JTranscInput_.impl.onMouseDown_Ljtransc_JTranscInput_MouseInfo__V(jtransc.JTranscInput_.mouseInfo);
	}
	override public function onMouseMove (window:lime.ui.Window, x:Float, y:Float):Void {
		jtransc.JTranscInput_.mouseInfo.x = Std.int(x);
		jtransc.JTranscInput_.mouseInfo.y = Std.int(y);
		jtransc.JTranscInput_.impl.onMouseMove_Ljtransc_JTranscInput_MouseInfo__V(jtransc.JTranscInput_.mouseInfo);
	}
}