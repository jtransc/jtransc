package jtransc.annotation;

import com.jtransc.annotation.haxe.HaxeMethodBody;

public class MethodBodyTest {
    static public void main(String[] args) {
        System.out.println(mymethod(777));
    }

    @HaxeMethodBody("return N.str('INT:$p0');")
    static public native String mymethod(int arg);
}
