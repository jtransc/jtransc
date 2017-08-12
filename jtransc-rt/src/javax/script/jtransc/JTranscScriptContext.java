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
package javax.script.jtransc;

import javax.script.Bindings;
import javax.script.ScriptContext;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public class JTranscScriptContext implements ScriptContext {
	@Override
	public void setBindings(Bindings bindings, int scope) {

	}

	@Override
	public Bindings getBindings(int scope) {
		return null;
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {

	}

	@Override
	public Object getAttribute(String name, int scope) {
		return null;
	}

	@Override
	public Object removeAttribute(String name, int scope) {
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return null;
	}

	@Override
	public int getAttributesScope(String name) {
		return 0;
	}

	@Override
	public Writer getWriter() {
		return null;
	}

	@Override
	public Writer getErrorWriter() {
		return null;
	}

	@Override
	public void setWriter(Writer writer) {

	}

	@Override
	public void setErrorWriter(Writer writer) {

	}

	@Override
	public Reader getReader() {
		return null;
	}

	@Override
	public void setReader(Reader reader) {

	}

	@Override
	public List<Integer> getScopes() {
		return null;
	}
}
