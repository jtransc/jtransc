package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@Repeatable(value = JTranscAddImportsList.class)
public @interface JTranscAddImports {
	String target();

	String cond() default "";

	String[] value();
}