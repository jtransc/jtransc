package javax.script.jtransc;

import com.jtransc.text.JTranscReaderTools;

import javax.script.*;
import java.io.Reader;

abstract public class JTranscScriptEngine implements ScriptEngine {
	private ScriptEngineFactory factory = new JTranscScriptEngineFactory();
	private ScriptContext context = new JTranscScriptContext();

	@Override
	abstract public Object eval(String script, ScriptContext context) throws ScriptException;

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		return eval(JTranscReaderTools.readAllOrNull(reader), context);
	}

	@Override
	public Object eval(String script) throws ScriptException {
		return eval(script, context);
	}

	@Override
	public Object eval(Reader reader) throws ScriptException {
		return null;
	}

	@Override
	public Object eval(String script, Bindings n) throws ScriptException {
		context.setBindings(n, 0);
		return eval(script, context);
	}

	@Override
	public Object eval(Reader reader, Bindings n) throws ScriptException {
		context.setBindings(n, 0);
		return eval(JTranscReaderTools.readAllOrNull(reader), context);
	}

	@Override
	public void put(String key, Object value) {
	}

	@Override
	public Object get(String key) {
		return null;
	}

	@Override
	public Bindings getBindings(int scope) {
		return context.getBindings(scope);
	}

	@Override
	public void setBindings(Bindings bindings, int scope) {
		context.setBindings(bindings, scope);
	}

	@Override
	public Bindings createBindings() {
		return new JTranscBindings();
	}

	@Override
	public ScriptContext getContext() {
		return context;
	}

	@Override
	public void setContext(ScriptContext context) {
		this.context = context;
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}
}
