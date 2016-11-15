package j;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.JTranscNativeName;
import com.jtransc.annotation.JTranscVisible;

@SuppressWarnings({"unused", "WeakerAccess"})
@JTranscKeep
@JTranscVisible
public class ClassInfo {
	@JTranscInvisible
	public int id;
	@JTranscInvisible
	public String internalName;
	@JTranscInvisible
	public String name;
	@JTranscInvisible
	public int modifiers;
	@JTranscInvisible
	public int parent;
	@JTranscInvisible
	public int[] interfaces;
	@JTranscInvisible
	public int[] related;

	public ClassInfo(int id, String internalName, String name, int modifiers, int parent, int[] interfaces, int[] related) {
		if (internalName == null) internalName = name;
		this.id = id;
		this.internalName = internalName;
		this.name = name;
		this.modifiers = modifiers;
		this.parent = parent;
		this.interfaces = interfaces;
		this.related = related;
	}

	@JTranscKeep
	@JTranscNativeName("create")
	static public ClassInfo create(int id, String internalName, String name, int modifiers, int parent, int[] interfaces, int[] related) {
		return new ClassInfo(id, internalName, name, modifiers, parent, interfaces, related);
	}
}
