package issues;

import com.jtransc.annotation.JTranscKeep;

@JTranscKeep
public class Issue209 {

    static private final boolean TRUE = true;
    static private final boolean FALSE = false;

    static{
        assert TRUE;
        assert !FALSE;
    }

    static public void main(String[] args) {
        System.out.println(TRUE);
        System.out.println(FALSE);
    }
}
