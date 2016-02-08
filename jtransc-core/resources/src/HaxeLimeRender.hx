import lime.app.Application;
import lime.graphics.Renderer;
import lime.graphics.FlashRenderContext;
import lime.graphics.GLRenderContext;

class HaxeLimeRender {
    static public var impl:HaxeLimeRenderImpl;
    static public var width:Int = 640;
    static public var height:Int = 480;

    static public function setRenderer(renderer:Renderer) {
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