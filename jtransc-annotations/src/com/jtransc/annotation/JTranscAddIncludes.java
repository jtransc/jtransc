package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@Repeatable(value = JTranscAddIncludesList.class)
public @interface JTranscAddIncludes {
	String target();

	String cond() default "";

	String[] value();
}