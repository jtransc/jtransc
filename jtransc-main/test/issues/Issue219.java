package issues;

import com.jtransc.annotation.JTranscKeep;

@JTranscKeep
public class Issue219 {

	static public void main(String[] args) {
		String[] strArr = new String[]{"1"};
		int i = strArr.length - 1;
		while (i >= 0) {
			if ("1".equals(strArr[i--])) System.out.println("OK!");
		}
	}
}
