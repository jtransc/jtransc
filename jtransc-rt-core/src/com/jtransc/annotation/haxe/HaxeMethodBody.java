package com.jtransc.annotation.haxe;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Repeatable(value = HaxeMethodBodyList.class)
public @interface HaxeMethodBody {
	String value();
	String target() default "";
}