package javax.script;

import java.io.Reader;
import java.io.Writer;
import java.util.List;

public interface ScriptContext {
	public static final int ENGINE_SCOPE = 100;
	public static final int GLOBAL_SCOPE = 200;

	public void setBindings(Bindings bindings, int scope);

	public Bindings getBindings(int scope);

	public void setAttribute(String name, Object value, int scope);

	public Object getAttribute(String name, int scope);

	public Object removeAttribute(String name, int scope);

	public Object getAttribute(String name);

	public int getAttributesScope(String name);

	public Writer getWriter();

	public Writer getErrorWriter();

	public void setWriter(Writer writer);

	public void setErrorWriter(Writer writer);

	public Reader getReader();

	public void setReader(Reader reader);

	public List<Integer> getScopes();
}
