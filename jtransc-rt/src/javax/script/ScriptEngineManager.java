/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package javax.script;

import javax.script.jtransc.impl.JSJtranscScriptEngine;
import java.util.List;

public class ScriptEngineManager {
	public ScriptEngineManager() {
	}

	public ScriptEngineManager(ClassLoader loader) {
	}

	native public void setBindings(Bindings bindings);

	native public Bindings getBindings();

	native public void put(String key, Object value);

	native public Object get(String key);

	public ScriptEngine getEngineByName(String shortName) {
		return new JSJtranscScriptEngine();
	}

	public ScriptEngine getEngineByExtension(String extension) {
		return new JSJtranscScriptEngine();
	}

	public ScriptEngine getEngineByMimeType(String mimeType) {
		return new JSJtranscScriptEngine();
	}

	native public List<ScriptEngineFactory> getEngineFactories();

	native public void registerEngineName(String name, ScriptEngineFactory factory);

	native public void registerEngineMimeType(String type, ScriptEngineFactory factory);

	native public void registerEngineExtension(String extension, ScriptEngineFactory factory);
}