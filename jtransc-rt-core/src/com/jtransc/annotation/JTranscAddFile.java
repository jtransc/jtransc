package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(value = JTranscAddFileList.class)
public @interface JTranscAddFile {
	int priority() default 0;

	String target();

	boolean process();

	String prepend() default "";

	String append() default "";

	String file() default "";
}