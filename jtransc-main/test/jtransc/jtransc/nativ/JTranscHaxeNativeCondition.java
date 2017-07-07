package jtransc.jtransc.nativ;

import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.io.JTranscConsole;

public class JTranscHaxeNativeCondition {
	static public void main(String[] args) {
		JTranscConsole.log(showFPSDoubleIf());
		JTranscConsole.log(showFPSElse());
		JTranscConsole.log(getFramesPerSecondDoubleIf());
		JTranscConsole.log(getFramesPerSecondElse());
	}

	@HaxeMethodBody(
		"{% if extra.showFPS %} return {{ extra.showFPS }}; {% end %}" +
			"{% if !extra.showFPS %} return false; {% end %}"
	)
	native static public boolean showFPSDoubleIf();

	@HaxeMethodBody(
		"{% if extra.showFPS %} return {{ extra.showFPS }}; " +
			"{% else %} return false; {% end %}"
	)
	native static public boolean showFPSElse();

	@HaxeMethodBody("" +
		"{% if extra.fps %} return {{ extra.fps }};" +
		"{% else %} return -1;" +
		"{% end %}"
	)
	native public static int getFramesPerSecondElse();

	@HaxeMethodBody(
		"{% if extra.fps %}return {{ extra.fps }};{% end %}" +
			"{% if !extra.fps %}return -1;{% end %}"
	)
	native public static int getFramesPerSecondDoubleIf();
}
