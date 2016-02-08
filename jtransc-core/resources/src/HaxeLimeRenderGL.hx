import lime.app.Application;
import lime.graphics.Renderer;
import lime.graphics.GLRenderContext;
import lime.graphics.opengl.*;
import lime.utils.*;
import lime.Assets;
import lime.math.Matrix4;

class HaxeLimeRenderGL extends HaxeLimeRenderImpl {
    public var gl:GLRenderContext;

	private var glMatrixUniform:GLUniformLocation;
	private var glImageUniform:GLUniformLocation;
	private var glProgram:GLProgram;
	private var glTexture:GLTexture;
	private var glTextureAttribute:Int;
	private var glVertexAttribute:Int;
	private var glColor0Attribute:Int;
	private var glColor1Attribute:Int;
	private var textures:Array<GLTexture>;
	private var textureIndices:Array<Int>;

	private var indicesBuffer:GLBuffer = null;
	private var verticesBuffer:GLBuffer = null;

    public function new(gl:GLRenderContext) {
        this.gl = gl;

        textureIndices = [for (i in 1...1024) i];
        textures = [for (i in 0...1024) null];

        init();
    }

    private function init() {
        var vertexSource = "
            attribute vec2 aPosition;
            attribute vec2 aTexCoord;
            //attribute vec4 aColor0;
            //attribute vec4 aColor1;
            varying vec2 vTexCoord;
            //varying vec4 vColor0;
            //varying vec4 vColor1;

            uniform mat4 uMatrix;

            void main(void) {
                vTexCoord = aTexCoord;
                //vColor0 = aColor0;
                //vColor1 = aColor1;
                gl_Position = uMatrix * vec4(aPosition, 0, 1);
            }
        ";

        var fragmentSource = "" +
            #if !desktop
            "precision mediump float;" +
            #end
            "
            varying vec2 vTexCoord;
            //varying vec4 vColor0;
            //varying vec4 vColor1;
            uniform sampler2D uImage0;

            void main(void) {
                gl_FragColor = texture2D(uImage0, vTexCoord);
                gl_FragColor = gl_FragColor;
                //gl_FragColor = vec4(1, 1, 0, 1);
            }
        ";

        glProgram = GLUtils.createProgram (vertexSource, fragmentSource);
        gl.useProgram(glProgram);

        // Attributes
        glVertexAttribute = gl.getAttribLocation(glProgram, "aPosition");
        glTextureAttribute = gl.getAttribLocation(glProgram, "aTexCoord");
        glColor0Attribute = gl.getAttribLocation(glProgram, "aColor0");
        glColor1Attribute = gl.getAttribLocation(glProgram, "aColor1");

        // Uniforms
        glMatrixUniform = gl.getUniformLocation(glProgram, "uMatrix");
        glImageUniform = gl.getUniformLocation(glProgram, "uImage0");
    }

    override public function isInitialized() {
        return true;
    }

    override public function createTexture(path:String, width:Int, height:Int):Int {
        trace('HaxeLimeRenderGL.createTexture($path)');
        //path = 'assets/image.png';
        //trace('HaxeLimeRenderGL.createTexture[2]($path)');
        var image = Assets.getImage(path);
        var id = textureIndices.pop();
        var glTexture = textures[id] = gl.createTexture();

        gl.bindTexture(gl.TEXTURE_2D, glTexture);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
        #if js
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image.src);
        #else
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, image.buffer.width, image.buffer.height, 0, gl.RGBA, gl.UNSIGNED_BYTE, image.data);
        #end
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.LINEAR);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
        gl.bindTexture(gl.TEXTURE_2D, null);


        return id;
    }

    override public function disposeTexture(id:Int) {
        trace('HaxeLimeRenderGL.disposeTexture(id)');
        gl.deleteTexture(textures[id]);
        textures[id] = null;
        textureIndices.push(id);
    }

    override public function render(
        width:Int, height:Int,
        _vertices:haxe.io.Float32Array, vertexCount:Int,
        _indices:haxe.io.UInt16Array, indexCount:Int,
        _batches:haxe.io.Int32Array, batchCount:Int
    ) {
        var indicesData = new lime.utils.UInt16Array(indexCount);
        var verticesData = new lime.utils.Float32Array(vertexCount * 6);
        //trace('----');
        for (n in 0 ... indexCount) {
            indicesData[n] = _indices[n];
            //trace('index[$n]:' + indicesData[n]);
        }
        for (n in 0 ... vertexCount * 6) {
            verticesData[n] = _vertices[n];
            //trace('vertex[$n]:' + verticesData[n]);
        }

        //trace('render:' + width + 'x' + height);
        //trace('render:' + vertexCount + ',' + indexCount + "," + batchCount);

        gl.viewport(0, 0, width, height);

        gl.clearColor(0.2, 0.2, 0.2, 1.0);
        gl.clear(gl.COLOR_BUFFER_BIT);

        gl.useProgram(glProgram);

        var matrix = Matrix4.createOrtho(0, width, height, 0, -1000, 1000);
        gl.uniformMatrix4fv(glMatrixUniform, false, matrix);
        gl.uniform1i(glImageUniform, 0);

        if (indicesBuffer == null) indicesBuffer = gl.createBuffer();
        if (verticesBuffer == null) verticesBuffer = gl.createBuffer();

        gl.bindBuffer(gl.ARRAY_BUFFER, verticesBuffer);
        gl.bufferData(gl.ARRAY_BUFFER, verticesData, gl.STATIC_DRAW);

        gl.enableVertexAttribArray(glVertexAttribute);
        gl.enableVertexAttribArray(glTextureAttribute);
        //gl.enableVertexAttribArray(glColor0Attribute);
        //gl.enableVertexAttribArray(glColor1Attribute);

        var STRIDE = 6 * 4;
        gl.bindBuffer(gl.ARRAY_BUFFER, verticesBuffer);
        gl.vertexAttribPointer(glVertexAttribute, 2, gl.FLOAT, false, STRIDE, 0 * 4);
        gl.vertexAttribPointer(glTextureAttribute, 2, gl.FLOAT, false, STRIDE, 2 * 4);
        //gl.vertexAttribPointer(glColor0Attribute, 4, gl.UNSIGNED_BYTE, false, STRIDE, 4 * 4);
        //gl.vertexAttribPointer(glColor1Attribute, 4, gl.UNSIGNED_BYTE, false, STRIDE, 5 * 4);

        gl.enable(gl.BLEND);
        #if desktop gl.enable(gl.TEXTURE_2D); #end

        gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, indicesBuffer);
        gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, indicesData, gl.STATIC_DRAW);

        for (batchId in 0 ... batchCount) {
            var batchOffset = batchId * 16;
            var indexStart    = _batches[batchOffset + 0];
            var triangleCount = _batches[batchOffset + 1];
            var textureId     = _batches[batchOffset + 2];
            var blendMode     = _batches[batchOffset + 3];
            var maskType      = _batches[batchOffset + 4];
            var stencilIndex  = _batches[batchOffset + 5];
            var scissorLeft   = _batches[batchOffset + 6];
            var scissorTop    = _batches[batchOffset + 7];
            var scissorRight  = _batches[batchOffset + 8];
            var scissorBottom = _batches[batchOffset + 9];

            var glTexture = textures[textureId];

            gl.activeTexture(gl.TEXTURE0);
            gl.bindTexture(gl.TEXTURE_2D, glTexture);

            var premultiplied = false;
            if (!premultiplied) {
                switch (blendMode) {
                    case HaxeLimeRenderImpl.BLEND_NONE    : gl.blendFunc(gl.ONE, gl.ZERO);
                    case HaxeLimeRenderImpl.BLEND_NORMAL  : gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
                    case HaxeLimeRenderImpl.BLEND_ADD     : gl.blendFunc(gl.SRC_ALPHA, gl.ONE);
                    case HaxeLimeRenderImpl.BLEND_MULTIPLY: gl.blendFunc(gl.DST_COLOR, gl.ONE_MINUS_SRC_COLOR);
                    case HaxeLimeRenderImpl.BLEND_SCREEN  : gl.blendFunc(gl.SRC_ALPHA, gl.ONE);
                    case HaxeLimeRenderImpl.BLEND_ERASE   : gl.blendFunc(gl.ZERO, gl.ONE_MINUS_SRC_ALPHA);
                    case HaxeLimeRenderImpl.BLEND_BELOW   : gl.blendFunc(gl.ONE_MINUS_DST_ALPHA, gl.DST_ALPHA);
                    default: gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
                }
            } else {
                switch (blendMode) {
                    case HaxeLimeRenderImpl.BLEND_NONE    : gl.blendFunc(gl.ONE, gl.ZERO);
                    case HaxeLimeRenderImpl.BLEND_NORMAL  : gl.blendFunc(gl.ONE, gl.ONE_MINUS_SRC_ALPHA);
                    case HaxeLimeRenderImpl.BLEND_ADD     : gl.blendFunc(gl.ONE, gl.ONE);
                    case HaxeLimeRenderImpl.BLEND_MULTIPLY: gl.blendFunc(gl.DST_COLOR, gl.ONE_MINUS_SRC_ALPHA);
                    case HaxeLimeRenderImpl.BLEND_SCREEN  : gl.blendFunc(gl.ONE, gl.ONE_MINUS_SRC_COLOR);
                    case HaxeLimeRenderImpl.BLEND_ERASE   : gl.blendFunc(gl.ZERO, gl.ONE_MINUS_SRC_ALPHA);
                    case HaxeLimeRenderImpl.BLEND_BELOW   : gl.blendFunc(gl.ONE_MINUS_DST_ALPHA, gl.DST_ALPHA);
                    default: gl.blendFunc(gl.ONE, gl.ONE_MINUS_SRC_ALPHA);
                }
            }

            //trace('batch:' + indexStart + ',' + triangleCount);

            gl.drawElements(gl.TRIANGLES, triangleCount * 3, gl.UNSIGNED_SHORT, indexStart * 2);
        }

        //gl.deleteBuffer(verticesBuffer);
        //gl.deleteBuffer(indicesBuffer);
    }
}