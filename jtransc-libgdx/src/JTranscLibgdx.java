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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import jtransc.JTranscAudio;
import jtransc.JTranscEventLoop;
import jtransc.JTranscRender;
import jtransc.JTranscVersion;

public class JTranscLibgdx {
	static private Application app;
	static private Runnable r_update;
	static private Runnable r_render;

	static private void init(final Runnable init) {
		JTranscRender.impl = new LibgdxRenderer();
		JTranscAudio.impl = new LibgdxAudio();
		LibgdxInput.config();
		if (init != null) init.run();
	}

	static private void frame() {
		if (r_update != null) r_update.run();
		if (r_render != null) r_render.run();
	}

	static public void config() {
		JTranscEventLoop.impl = new JTranscEventLoop.Impl() {
			@Override
			public void init(final Runnable init) {
				final int width = 640;
				final int height = 480;
				final String title = "JTransc " + JTranscVersion.getVersion();

				ApplicationAdapter applicationAdapter = new ApplicationAdapter() {
					@Override
					public void create() {
						JTranscLibgdx.init(init);
					}

					@Override
					public void render() {
						JTranscLibgdx.frame();
					}
				};

				//app = initLwjgl3(width, height, title, applicationAdapter);
				app = initLwjgl2(width, height, title, applicationAdapter);
				//app = initJglfw(width, height, title, applicationAdapter);
			}

			/*
			private Application initJglfw(final int width, final int height, final String title, final ApplicationAdapter appAdapter) {
				JglfwApplicationConfiguration config = new JglfwApplicationConfiguration();
				config.width = width;
				config.height = height;
				config.title = title;
				config.stencil = 8;
				return new JglfwApplication(appAdapter, config);
			}
			*/

			private Application initLwjgl2(final int width, final int height, final String title, final ApplicationAdapter appAdapter) {
				LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
				config.width = width;
				config.height = height;
				config.title = title;
				config.stencil = 8;
				return new LwjglApplication(appAdapter, config);
			}

			/*
			private Application initLwjgl3(final int width, final int height, final String title, final ApplicationAdapter appAdapter) {
				Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
				config.setWindowedMode(width, height);
				config.setTitle(title);
				//config.stencil = 8;
				return new Lwjgl3Application(appAdapter, config);
			}
			*/

			@Override
			public void loop(Runnable update, Runnable render) {
				JTranscLibgdx.r_update = update;
				JTranscLibgdx.r_render = render;
			}
		};
	}
}
