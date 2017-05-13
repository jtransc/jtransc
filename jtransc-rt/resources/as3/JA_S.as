package {
import avm2.intrinsics.memory.sxi16;
public class JA_S extends JA_I {
	public function JA_S(length: int) { super(length, '[S'); }

	override public function get(index: int): int {
		return sxi16(data[index]);
	}
}
}