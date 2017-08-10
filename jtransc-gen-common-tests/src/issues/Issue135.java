package issues;

import java.io.PrintWriter;

public class Issue135 {
	
    static final PrintWriter SYSTEM_OUT_WRITER = new PrintWriter(System.out);
	
	static public void main(String[] args) throws Throwable {
        SYSTEM_OUT_WRITER.println("boom!");
        SYSTEM_OUT_WRITER.flush();
	}
}