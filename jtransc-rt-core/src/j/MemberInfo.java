package j;

import com.jtransc.annotation.*;

@SuppressWarnings("unused")
@JTranscKeep
@JTranscVisible
public class MemberInfo {
	@JTranscInvisible
	public int id;
	@JTranscInvisible
	public String internalName;
	@JTranscInvisible
	public String name;
	@JTranscInvisible
	public int modifiers;
	@JTranscInvisible
	public String desc;
	@JTranscInvisible
	public String genericDesc;

	@JTranscSync
	public MemberInfo(int id, String internalName, String name, int modifiers, String desc, String genericDesc) {
		this.internalName = (internalName != null) ? internalName : name;
		this.id = id;
		this.name = name;
		this.modifiers = modifiers;
		this.desc = desc;
		this.genericDesc = genericDesc;
	}

	@JTranscKeep
	@JTranscNativeName("c")
	@JTranscSync
	static public MemberInfo create(int id, String internalName, String name, int modifiers, String desc, String genericDesc) {
		return new MemberInfo(id, internalName, name, modifiers, desc, genericDesc);
	}

	@JTranscKeep
	@JTranscNativeName("cl")
	@JTranscSync
	static public MemberInfo[] createList(int count, int[] ids, int[] modifiers, String[] internalNames, String[] names, String[] descs, String[] genericDescs) {
		MemberInfo[] out = new MemberInfo[count];
		for (int n = 0; n < count; n++) {
			out[n] = new MemberInfo(ids[n], internalNames[n], names[n], modifiers[n], descs[n], genericDescs[n]);
		}
		return out;
	}
}
