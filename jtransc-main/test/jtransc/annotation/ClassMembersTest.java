package jtransc.annotation;

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({"static public var mynativeField:Int = 123;" })
public class ClassMembersTest {
    static public void main(String[] args) {
        System.out.println("mult:" + mult(2));
    }

    @HaxeMethodBody("return mynativeField * p0;")
    static public native int mult(int p0);
}
