package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface JTranscRegisterCommandList {
	JTranscRegisterCommand[] value() default {};
}
