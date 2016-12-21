package javax.script.jtransc;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Map;

public class JTranscBindings extends HashMap<String, Object> implements Bindings {
	public Object put(String name, Object value) {
		return super.put(name, value);
	}

	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		super.putAll(toMerge);
	}

	public boolean containsKey(Object key) {
		return super.containsKey(key);
	}

	public Object get(Object key) {
		return super.get(key);
	}

	public Object remove(Object key) {
		return super.remove(key);
	}
}
