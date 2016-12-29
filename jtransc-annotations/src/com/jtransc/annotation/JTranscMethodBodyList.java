package com.jtransc.annotation;

import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface JTranscMethodBodyList {
	JTranscMethodBody[] value() default {};
}
