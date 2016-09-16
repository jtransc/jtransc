package big;

import com.jtransc.target.Cpp;

public class CppApi extends Api {
	@Override
	public int demo() {
		return Cpp.i_raw("7");
	}
}
