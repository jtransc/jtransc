package java.lang;

import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;

@JTranscAddMembers(target = "cs", value = "static public int __lastId = 1;")
public class SystemInt {
	@JTranscSync
	@JTranscMethodBody(target = "cpp", value = "return (int)(uintptr_t)(void *)p0;")
	@JTranscMethodBody(target = "js", value = "if (p0 == null) return 0; if (p0.$$id == 0) p0.$$id = N.nextIdentityHashCodeId(); return p0.$$id;")
	@JTranscMethodBody(target = "cs", value = "if (p0 == null) return 0; if (p0.__id == 0) p0.__id = __lastId++; return p0.__id;")
	native static public int identityHashCode(Object x);
}
