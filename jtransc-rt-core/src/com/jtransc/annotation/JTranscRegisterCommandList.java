package com.jtransc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface JTranscRegisterCommandList {
	JTranscRegisterCommand[] value() default {};
}
