package jtransc;

import com.jtransc.JTranscWrapped;
import com.jtransc.annotation.haxe.HaxeMethodBody;

public class WrappedTest {
	static public void main(String[] args) {
		System.out.println(getWrapped().get("a"));
	}

	@HaxeMethodBody("return HaxeNatives.wrap({a : 10 });")
	static private JTranscWrapped getWrapped() {
		return new JTranscWrapped(new Object() {
			public int a = 10;
		});
	}
}
