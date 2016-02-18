package all.media.as3;

import all.as3.As3Lib;
import all.core.AllProcedure1;
import all.media.*;
import flash.display.Stage;
import flash.display.Stage3D;
import flash.display.StageAlign;
import flash.display.StageScaleMode;
import flash.display3D.*;
import flash.display3D.textures.Texture;
import flash.events.Event;

public class As3MediaEngine extends AllMediaEngine {
    As3RenderContext renderContext = null;
    Context3D context3D;
    boolean initialized = false;

    public void start(final AllMediaHandler handler) {
        final Stage stage = As3Lib.stage;
        final Stage3D stage3D = stage.getStage3Ds()[0];
        stage.setAlign(StageAlign.TOP_LEFT);
        stage.setScaleMode(StageScaleMode.NO_SCALE);
        stage3D.addEventListener(Event.CONTEXT3D_CREATE, new AllProcedure1<Event>() {
            public void execute(Event e) {
                context3D = stage3D.getContext3D();
                renderContext = new As3RenderContext(context3D);
                context3D.configureBackBuffer(stage.getStageWidth(), stage.getStageHeight(), 4, true);
            }
        });
        stage3D.requestContext3D();
        As3Lib.stage.addEventListener(Event.ENTER_FRAME, new AllProcedure1<Event>() {
            public void execute(Event e) {
                if (renderContext == null) return;
                if (!initialized) {
                    handler.init(renderContext);
                    initialized = true;
                }
                context3D.clear(1.0, 1.0, 0.0, 1.0);
                handler.render(renderContext);
                context3D.present();
            }
        });
    }
}

class As3RenderContext implements AllRenderContext {
    public Context3D context3D;

    public As3RenderContext(Context3D context3D) {
        this.context3D = context3D;
    }

    public AllRenderProgram getSolidColorProgram() {
        return null;
    }

    public AllRenderProgram getTextureProgram() {
        return null;
    }

    public AllRenderTexture createTexture(AllTextureSource source) {
        return new As3RenderTexture(source, context3D.createTexture(source.getWidth(), source.getHeight(), Context3DTextureFormat.BGRA, false));
    }

    public IndexBuffer createIndexBuffer(int numIndices) {
        return new As3IndexBuffer(context3D.createIndexBuffer(numIndices));
    }

    public VertexBuffer createVertexBuffer(int numVertices, int data32PerVertex) {
        return new As3VertexBuffer(context3D.createVertexBuffer(numVertices, data32PerVertex));
    }

    public void drawTriangles(AllRenderTexture texture, AllRenderProgram program, IndexBuffer indices, VertexBuffer vertices, int indexStart, int count) {
        drawTriangles((As3RenderTexture)texture, (As3RenderProgram)program, (As3IndexBuffer)indices, (As3VertexBuffer)vertices, indexStart, count);
    }

    public void drawTriangles(As3RenderTexture texture, As3RenderProgram program, As3IndexBuffer indices, As3VertexBuffer vertices, int indexStart, int count) {
        if (texture != null) {
            context3D.setTextureAt(0, texture.texture);
        } else {
            context3D.setTextureAt(0, null);
        }
        context3D.setProgram(program.program);
        context3D.setVertexBufferAt(0, vertices.buffer);
        context3D.drawTriangles(indices.buffer, indexStart, count);
    }
}

class As3RenderProgram implements AllRenderProgram {
    public Program3D program;

    public As3RenderProgram(Program3D program) {
        this.program = program;
    }
}

class As3RenderTexture extends AllRenderTexture {
    public Texture texture;

    public As3RenderTexture(AllTextureSource textureSource, Texture texture) {
        super(textureSource);
        this.texture = texture;
    }

    public void dispose() {
        texture.dispose();
    }
}

class As3IndexBuffer implements IndexBuffer {
    public IndexBuffer3D buffer;

    public As3IndexBuffer(IndexBuffer3D buffer) {
        this.buffer = buffer;
    }

    public void upload(int[] data, int start, int count) {
        buffer.uploadFromVector(data, start, count);
    }

    public void dispose() {
        buffer.dispose();
    }
}

class As3VertexBuffer implements VertexBuffer {
    public VertexBuffer3D buffer;

    public As3VertexBuffer(VertexBuffer3D buffer) {
        this.buffer = buffer;
    }

    public void upload(float[] data, int start, int count) {
        // @TODO: This should work without copying since as3 doesn't have floats and just Number (that is double)
        double[] data2 = new double[data.length];
        for (int n = 0; n < data2.length; n++) data2[n] = data[n];
        buffer.uploadFromVector(data2, start, count);
    }

    public void dispose() {
        buffer.dispose();
    }
}
