package example;

import com.jtransc.JTranscSystem;
import com.jtransc.JTranscVersion;

public class Test {
    static public void main(String[] args) {
        System.out.println("Hello World! " + JTranscVersion.getVersion() + ", " + JTranscVersion.getRuntime() + ", " + JTranscSystem.usingJTransc());
    }
}