package com.jtransc.annotation.haxe;

import java.lang.annotation.*;

@Repeatable(value = HaxeMethodBodyList.class)
public @interface HaxeMethodBodyEntry {
	String value();
	String target() default "";
}