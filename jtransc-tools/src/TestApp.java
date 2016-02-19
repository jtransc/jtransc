import lime.app.Application;
import lime.app.IModule;

class HaxeLimeJTranscApplication extends lime.app.Application {
    @Override
    public void onPreloadComplete() {
        //switch (renderer.context) {
        //	case FLASH(sprite): #if flash initializeFlash(sprite); #end
        //	case OPENGL (gl):
        //	default:
        //	throw "Unsupported render context";
        //}
    }

    private boolean initialized = false;
    private boolean initializedRenderer = false;

    @Override
    public void render(lime.graphics.Renderer renderer) {
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

    @Override
    public void update(int deltaTime) {
        super.update(deltaTime);
        if (HaxeLimeRender.isInitialized()) {
            if (HaxeNatives.updateHandler != null) HaxeNatives.updateHandler();
        }
    }

    public HaxeLimeJTranscApplication() {
        super();
        HaxeNatives.enabledDefaultEventLoop = false;
        addModule(new JTranscModule());
    }
}

class JTranscModule extends lime.app.Module {
    @Override
    public void onMouseUp (lime.ui.Window window, double x, double y, int button) {
        jtransc.JTranscInput.mouseInfo.x = (int)(x);
        jtransc.JTranscInput.mouseInfo.y = (int)(y);
        jtransc.JTranscInput.mouseInfo.buttons &= ~(1 << button);
        jtransc.JTranscInput.impl.onMouseUp(jtransc.JTranscInput.mouseInfo);
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

class HaxeLimeRender {
    static public HaxeLimeRenderImpl impl;
    static public int width = 640;
    static public int height = 480;

    static public void setRenderer(Renderer renderer) {
        if (HaxeLimeRender.impl != null) return;
        HaxeLimeRender.impl = switch (renderer.context) {
            #if flash
            case FLASH(sprite): new HaxeLimeRenderFlash(sprite);
                #else
            case OPENGL(gl): new HaxeLimeRenderGL(gl);
                #end
            default: throw 'Not supported renderer $renderer';
        }
    }

    static public function isInitialized() {
        if (HaxeLimeRender.impl == null) return false;
        return HaxeLimeRender.impl.isInitialized();
    }

    static public function setSize(width:Int, height:Int) {
        HaxeLimeRender.width = width;
        HaxeLimeRender.height = height;
    }

    static public function createTexture(path:String, width:Int, height:Int):Int {
        return impl.createTexture(path, width, height);
    }

    static public function disposeTexture(id:Int):Void {
        return impl.disposeTexture(id);
    }

    static public function render(
            _vertices:haxe.io.Float32Array, vertexCount:Int,
            _indices:haxe.io.UInt16Array, indexCount:Int,
            _batches:haxe.io.Int32Array, batchCount:Int
    ):Void {
        return impl.render(
                width, height,
                _vertices, vertexCount,
                _indices, indexCount,
                _batches, batchCount
        );
    }
}