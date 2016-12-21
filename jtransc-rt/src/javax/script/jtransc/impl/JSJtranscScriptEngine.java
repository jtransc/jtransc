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
		"return N.box(result);",
	})
	native public Object eval(String script, ScriptContext context) throws ScriptException;
}
