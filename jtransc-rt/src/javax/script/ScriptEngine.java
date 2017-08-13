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

import java.io.Reader;

public interface ScriptEngine {
	String ARGV = "javax.script.argv";
	String FILENAME = "javax.script.filename";
	String ENGINE = "javax.script.engine";
	String ENGINE_VERSION = "javax.script.engine_version";
	String NAME = "javax.script.name";
	String LANGUAGE = "javax.script.language";
	String LANGUAGE_VERSION = "javax.script.language_version";

	Object eval(String script, ScriptContext context) throws ScriptException;

	Object eval(Reader reader, ScriptContext context) throws ScriptException;

	Object eval(String script) throws ScriptException;

	Object eval(Reader reader) throws ScriptException;

	Object eval(String script, Bindings n) throws ScriptException;

	Object eval(Reader reader, Bindings n) throws ScriptException;

	void put(String key, Object value);

	Object get(String key);

	Bindings getBindings(int scope);

	void setBindings(Bindings bindings, int scope);

	Bindings createBindings();

	ScriptContext getContext();

	void setContext(ScriptContext context);

	ScriptEngineFactory getFactory();
}
