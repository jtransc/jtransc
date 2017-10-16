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
package javax.script.jtransc.impl;

import com.jtransc.annotation.JTranscMethodBody;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.jtransc.JTranscScriptEngine;

public class JSJtranscScriptEngine extends JTranscScriptEngine {
	@Override
	@JTranscMethodBody(target = "js", value = {
		//"console.log(N.istr(p0));",
		"var print = console.log; var result = eval(N.istr(p0));",
		"return N.box({{ JC_COMMA }}result);",
	})
	native public Object eval(String script, ScriptContext context) throws ScriptException;
}
