package jtransc.bug;

import jtransc.annotation.JTranscReferenceClass

@JTranscReferenceClass(
	"kotlin.reflect.KProperty\$Getter",
	"kotlin.reflect.KMutableProperty\$Setter"
)
object JTranscBug12Test2 {
	@JvmStatic fun main(args: Array<String>) {
		println("[1]");
		val field = Demo::field
		println("[2]");
	}

	class Demo {
		var field: String = "test"
	}
}

