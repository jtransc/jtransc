package impl;

import com.jtransc.annotation.JTranscTargetClassImpl;
import com.jtransc.annotation.JTranscTargetClassImplList

@JTranscTargetClassImplList(
	JTranscTargetClassImpl(target = "js", implementation = MyJsClass::class),
	JTranscTargetClassImpl(target = "cpp", implementation = MyCppClass::class)
)
class MyClassPerTarget {
	companion object {
		@JvmStatic fun str(): String = "default"
		@JvmStatic fun common(): String = "common"
	}
}

class MyJsClass {
	companion object {
		@JvmStatic fun str(): String = "js"
	}
}

class MyCppClass {
	companion object {
		@JvmStatic fun str(): String = "cpp"
	}
}