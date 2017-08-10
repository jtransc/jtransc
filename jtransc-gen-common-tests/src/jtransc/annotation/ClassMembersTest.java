package jtransc.annotation;

import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({"static public var mynativeField:Int = 123;" })
public class ClassMembersTest {
    static public void main(String[] args) {
        System.out.println("mult:" + mult(2));
    }

    @HaxeMethodBody("return mynativeField * p0;")
    static public native int mult(int p0);
}
