package jtransc.jtransc.js;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptEngineTest {
	static public void main(String[] args) {
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine js = sem.getEngineByName("JavaScript");
		try {
			Bindings bindings = js.createBindings();
			bindings.put("hello", "world");
			//js.eval("print('hello ' + hello + '!');", bindings);
			Object result = js.eval("print('hello world!') || 10;", bindings);
			System.out.println("result: " + result.getClass() + " : " + result);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}
}
