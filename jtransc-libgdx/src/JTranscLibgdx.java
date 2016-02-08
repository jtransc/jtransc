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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import jtransc.FastMemory;
import jtransc.JTranscEventLoop;
import jtransc.JTranscRender;

import java.util.Stack;

public class JTranscLibgdx {
	static private Lwjgl3Application app;
	static private Runnable r_update;
	static private Runnable r_render;

	static private void init() {
		JTranscRender.impl = new LibgdxRenderer();
	}

	static public void config() {
		JTranscEventLoop.impl = new JTranscEventLoop.Impl() {

			@Override
			public void init(final Runnable init) {
				Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
				config.setWindowedMode(640, 480);
				/*
				config.width = module.initialSize.width.toInt()
				config.height = module.initialSize.height.toInt()
				config.title = module.title
				config.stencil = 8
				log("DesktopAppInit: $module : ${config.width}x${config.height} : '${config.title}'")

				//LwjglApplication(app, config)
				JglfwApplication(app, config)
				*/

				app = new Lwjgl3Application(new ApplicationListener() {
					@Override
					public void create() {
						JTranscLibgdx.init();

						init.run();
					}

					@Override
					public void resize(int i, int i1) {

					}

					@Override
					public void render() {
						if (r_update != null) r_update.run();
						if (r_render != null) r_render.run();
					}

					@Override
					public void pause() {

					}

					@Override
					public void resume() {

					}

					@Override
					public void dispose() {

					}
				}, config);
			}

			@Override
			public void loop(Runnable update, Runnable render) {
				JTranscLibgdx.r_update = update;
				JTranscLibgdx.r_render = render;
			}
		};
	}
}

class LibgdxRenderer implements JTranscRender.Impl {
	Stack<Integer> textureIds = new Stack<Integer>();
	com.badlogic.gdx.graphics.Texture[] textures = new com.badlogic.gdx.graphics.Texture[2048];

	public LibgdxRenderer() {
		for (int n = 0; n < 2048; n++) textureIds.add(n);
	}

	@Override
	public int createTexture(String path, int width, int height) {
		int textureId = textureIds.pop();
		//textures[textureId] = new com.badlogic.gdx.graphics.Texture(path);
		return textureId;
	}

	@Override
	public void disposeTexture(int textureId) {
		textures[textureId].dispose();
		textures[textureId] = null;
		textureIds.push(textureId);
	}

	@Override
	public void render(FastMemory vertices, int vertexCount, short[] indices, int indexCount, int[] batches, int batchCount) {

	}
}
