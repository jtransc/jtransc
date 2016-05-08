package jtransc.jtransc;

import com.jtransc.annotation.haxe.HaxeCustomBuildCommandLine;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@HaxeCustomBuildCommandLine({
	"{{ defaultBuildCommand() }}",
	"-D", "custombuildtestwork",
	"@custombuildtest.cmd"
})
public class CustomBuildTest {
	@HaxeMethodBody(target = "custombuildtestwork", value = "return true;")
	@HaxeMethodBody("return false;")
	private static boolean worked1() {
		return true;
	}

	@HaxeMethodBody(target = "custombuildtestwork2", value = "return true;")
	@HaxeMethodBody("return false;")
	private static boolean worked2() {
		return true;
	}

	@HaxeMethodBody(target = "custombuildtestwork3", value = "return true;")
	@HaxeMethodBody("return false;")
	private static boolean worked3() {
		return false;
	}

	static public void main(String[] args) {
		System.out.println(worked1());
		System.out.println(worked2());
		System.out.println(worked3());
	}
}
