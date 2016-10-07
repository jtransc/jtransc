package j;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.JTranscNativeName;

@SuppressWarnings({"unused", "WeakerAccess"})
@JTranscKeep
@JTranscInvisible
public class ClassInfo {
	public int id;
	public String name;
	public int modifiers;
	public int parent;
	public int[] interfaces;
	public int[] related;

	public ClassInfo(int id, String name, int modifiers, int parent, int[] interfaces, int[] related) {
		this.id = id;
		this.name = name;
		this.modifiers = modifiers;
		this.parent = parent;
		this.interfaces = interfaces;
		this.related = related;
	}

	@JTranscKeep
	@JTranscNativeName("create")
	static public ClassInfo create(int id, String name, int modifiers, int parent, int[] interfaces, int[] related) {
		return new ClassInfo(id, name, modifiers, parent, interfaces, related);
	}
}
