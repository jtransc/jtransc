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