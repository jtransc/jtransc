package impl;

import com.jtransc.annotation.JTranscNativeName
import com.jtransc.annotation.JTranscTargetClassImpl;
import com.jtransc.annotation.JTranscTargetClassImplList

@JTranscTargetClassImplList(
	JTranscTargetClassImpl(target = "js", implementation = MyJsClass::class),
	JTranscTargetClassImpl(target = "cpp", implementation = MyCppClass::class)
)
open class MyClassPerTarget {
	companion object {
		@JvmStatic
		fun str(): String = "default"

		@JvmStatic
		fun common(): String = "common"
	}

	open fun demo() = "default.instance"
}

class MyJsClass : MyClassPerTarget() {
	companion object {
		@JvmStatic
		fun str(): String = "js" + MyJsDate(100.0).getTime().toInt()
	}

	override fun demo() = "js.instance"

	@JTranscNativeName("Date")
	class MyJsDate {
		constructor(time: Double)

		external fun getTime(): Double
	}
}

class MyCppClass : MyClassPerTarget() {
	companion object {
		@JvmStatic
		fun str(): String = "cpp"
	}

	override fun demo() = "cpp.instance"
}