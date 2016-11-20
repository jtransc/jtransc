package jtransc;

import com.jtransc.annotation.JTranscAddFile;
import com.jtransc.annotation.JTranscAddKeywords;
import com.jtransc.annotation.JTranscInvisibleExternal;
import com.jtransc.target.Js;

@JTranscAddFile(target = "js", prepend = "extraKeywords.js")
@JTranscAddKeywords(target = "js", value= { "A", "B", "C", "D", "E", "F", "G", "H", "I" })
public class ExtraKeywordsTest {
	static public void main(String[] args) {
		Js.v_raw("console.log(A);");
		Js.v_raw("console.log(B);");
		Js.v_raw("console.log(C);");
		Js.v_raw("console.log(D);");
		Js.v_raw("console.log(E);");
		Js.v_raw("console.log(F);");
		Js.v_raw("console.log(G);");
		Js.v_raw("console.log(H);");
		Js.v_raw("console.log(I);");
	}
}
