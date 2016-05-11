package jtransc.jtransc;

import com.jtransc.annotation.haxe.HaxeAddAssets;
import com.jtransc.annotation.haxe.HaxeCustomBuildCommandLine;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@HaxeCustomBuildCommandLine({
	"{{ defaultBuildCommand() }}",
	"-D", "custombuildtestwork",
	"@custombuildtest.cmd"
})
@HaxeAddAssets({"filetoinclude.txt"})
public class CustomBuildTest {
	@HaxeMethodBody(target = "custombuildtestwork", value = "return true;")
	@HaxeMethodBody("return false;")
	native private static boolean worked1();

	@HaxeMethodBody(target = "custombuildtestwork2", value = "return true;")
	@HaxeMethodBody("return false;")
	native private static boolean worked2();

	@HaxeAddAssets({"filetoinclude2.txt"})
	static class Demo {
		@HaxeMethodBody(target = "custombuildtestwork3", value = "return true;")
		@HaxeMethodBody("return false;")
		native private static boolean worked3();
	}

	@HaxeMethodBody(target = "custombuildtestwork_file1", value = "return true;")
	@HaxeMethodBody("return false;")
	native private static boolean worked_file1();

	@HaxeMethodBody(target = "custombuildtestwork_file2", value = "return true;")
	@HaxeMethodBody("return false;")
	native private static boolean worked_file2();

	@HaxeMethodBody(target = "custombuildtestwork_file3", value = "return true;")
	@HaxeMethodBody("return false;")
	native private static boolean worked_file3();

	static public void main(String[] args) {
		System.out.println(worked1());
		System.out.println(worked2());
		System.out.println(Demo.worked3());
		System.out.println(worked_file1());
		System.out.println(worked_file2());
		System.out.println(worked_file3());
	}
}
