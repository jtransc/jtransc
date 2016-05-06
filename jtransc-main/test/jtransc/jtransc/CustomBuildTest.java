package jtransc.jtransc;

import com.jtransc.annotation.haxe.HaxeCustomBuildCommandLine;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@HaxeCustomBuildCommandLine({
	"{{ defaultBuildCommand() }}",
	"-D", "custombuildtestwork",
})

public class CustomBuildTest {
	@HaxeMethodBody("#if custombuildtestwork return true; #else return false; #end")
	static public boolean worked() {
		return true;
	}

	//@HaxeMethodBodyEntry(value = "", target = "")
	//@HaxeMethodBodyEntry("")
	//@HaxeMethodBodyEntry("")
	static public void main(String[] args) {
		System.out.println(worked());
	}
}
