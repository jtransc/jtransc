package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@Repeatable(value = JTranscAddDefinesList.class)
public @interface JTranscAddDefines {
	String target();

	String cond() default "";

	String[] value();
}