package issues;

import javatest.TestStringTools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;

public class Issue105 {
	static public void main(String[] args) throws Throwable {
		InputStreamReader is = new InputStreamReader(new ByteArrayInputStream(new byte[]{'A', 'B', (byte) 0xC3, (byte) 0xA1}), "CP866");
		System.out.println("readLine:" + TestStringTools.escape(new BufferedReader(is).readLine()));
	}
}
