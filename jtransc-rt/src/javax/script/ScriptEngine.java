package javax.script;

import java.io.Reader;

public interface ScriptEngine {
	public static final String ARGV = "javax.script.argv";
	public static final String FILENAME = "javax.script.filename";
	public static final String ENGINE = "javax.script.engine";
	public static final String ENGINE_VERSION = "javax.script.engine_version";
	public static final String NAME = "javax.script.name";
	public static final String LANGUAGE = "javax.script.language";
	public static final String LANGUAGE_VERSION = "javax.script.language_version";

	public Object eval(String script, ScriptContext context) throws ScriptException;

	public Object eval(Reader reader, ScriptContext context) throws ScriptException;

	public Object eval(String script) throws ScriptException;

	public Object eval(Reader reader) throws ScriptException;

	public Object eval(String script, Bindings n) throws ScriptException;

	public Object eval(Reader reader, Bindings n) throws ScriptException;

	public void put(String key, Object value);

	public Object get(String key);

	public Bindings getBindings(int scope);

	public void setBindings(Bindings bindings, int scope);

	public Bindings createBindings();

	public ScriptContext getContext();

	public void setContext(ScriptContext context);

	public ScriptEngineFactory getFactory();
}
