package j;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscKeep;

@SuppressWarnings("unused")
@JTranscKeep
@JTranscInvisible
public class MemberInfo {
	public int id;
	public String name;
	public int modifiers;
	public String desc;
	public String genericDesc;

	public MemberInfo(int id, String name, int modifiers, String desc, String genericDesc) {
		this.id = id;
		this.name = name;
		this.modifiers = modifiers;
		this.desc = desc;
		this.genericDesc = genericDesc;
	}

	@JTranscKeep
	static public MemberInfo create(int id, String name, int modifiers, String desc, String genericDesc) {
		return new MemberInfo(id, name, modifiers, desc, genericDesc);
	}

	@JTranscKeep
	static public MemberInfo[] createList(int count, int[] ids, int[] modifiers, String[] names, String[] descs, String[] genericDescs) {
		MemberInfo[] out = new MemberInfo[count];
		for (int n = 0; n < count; n++) {
			out[n] = new MemberInfo(ids[n], names[n], modifiers[n], descs[n], genericDescs[n]);
		}
		return out;
	}
}
