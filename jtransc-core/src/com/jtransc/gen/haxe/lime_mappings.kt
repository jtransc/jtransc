/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.gen.haxe

import com.jtransc.ast.AstType
import com.jtransc.gen.ClassMappings

val LimeCopyFiles = HaxeCopyFiles + listOf(
	"AGALMiniAssembler.hx",
	"HaxeLimeRender.hx",
	"HaxeLimeRenderImpl.hx",
	"HaxeLimeRenderFlash.hx",
	"HaxeLimeRenderGL.hx"
)

fun LimeMappings():ClassMappings {
	val mappings = HaxeMappings()
	val FASTMEMORY = AstType.REF("jtransc.FastMemory")

	mappings.map("jtransc.JTranscRender") {
		body(INT, "createTexture", ARGS(STRING, INT, INT), "return HaxeLimeRender.createTexture(p0._str, p1, p2);")
		body(VOID, "disposeTexture", ARGS(INT), "HaxeLimeRender.disposeTexture(p0);")
		body(VOID, "render", ARGS(FASTMEMORY, INT, ARRAY(SHORT), INT, ARRAY(INT), INT), """
			HaxeLimeRender.render(p0.floatData, p1, p2.data, p3, p4.data, p5);
		""")
		//static public void render(float[] vertices, int vertexCount, short[] indices, int indexCount, int[] batches, int batchCount)
	}

	mappings.map("jtransc.JTranscIO") {
		val CALLBACK = AstType.REF("jtransc.JTranscCallback")
		body(VOID, "read", ARGS(STRING, CALLBACK), """
			var bytes = lime.Assets.getBytes(p0._str); // LIME >= 2.8
			//var byteArray = lime.Assets.getBytes(p0._str);
			//var bytes = haxe.io.Bytes.alloc(byteArray.length);
			//for (n in 0 ... byteArray.length) bytes.set(n, byteArray.__get(n));
			p1.handler_Ljava_lang_Throwable_Ljava_lang_Object__V(null, HaxeByteArray.fromBytes(bytes));
		""")
	}


	return mappings
}