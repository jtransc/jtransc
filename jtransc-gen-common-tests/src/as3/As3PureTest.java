package as3;

import com.jtransc.annotation.JTranscAddHeader;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscNativeName;
import com.jtransc.io.JTranscConsole;

public class As3PureTest {
	static public void main(String[] args) {
		//System.out.println(FlashSystemSystem.freeMemory);
		FlashSystemSystem.gc();
		FlashByteArray ba = FlashByteArrayUtils.create();
		ba.length = 1024;
		JTranscConsole.log(ba.length);
		ba.clear();
		JTranscConsole.log(ba.length);
	}
}

@JTranscNativeName("flash.system.System")
class FlashSystemSystem {
	static public double freeMemory;

	static native public void gc();
}

@JTranscAddHeader(target = "as3", value = "import flash.utils.ByteArray;")
class FlashByteArrayUtils {
	@JTranscMethodBody(target = "as3", value = "return new flash.utils.ByteArray();")
	native static public FlashByteArray create();
}

@JTranscNativeName("flash.utils.ByteArray")
class FlashByteArray {
	public FlashByteArray() {
	}

	public int length;

	native public void clear();
}