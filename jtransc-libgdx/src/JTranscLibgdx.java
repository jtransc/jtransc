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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import jtransc.*;

import java.util.Stack;

public class JTranscLibgdx {
	static private LwjglApplication app;
	static private Runnable r_update;
	static private Runnable r_render;

	static private void init() {
		JTranscRender.impl = new LibgdxRenderer();
	}

	static public void config() {
		JTranscEventLoop.impl = new JTranscEventLoop.Impl() {
			@Override
			public void init(final Runnable init) {
				LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

				config.width = 640;
				config.height = 480;
				config.title = "JTransc " + JTranscVersion.getVersion();
				config.stencil = 8;

				app = new LwjglApplication(new ApplicationAdapter() {
					@Override
					public void create() {
						JTranscLibgdx.init();
						init.run();
					}

					@Override
					public void render() {
						if (r_update != null) r_update.run();
						if (r_render != null) r_render.run();
					}
				}, config);

                LibgdxInput.config();
			}

			@Override
			public void loop(Runnable update, Runnable render) {
				JTranscLibgdx.r_update = update;
				JTranscLibgdx.r_render = render;
			}
		};
	}
}
