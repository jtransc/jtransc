package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(value = JTranscAddFileList.class)
public @interface JTranscAddFile {
	int priority() default 0;

	String target();

	boolean process() default false;

	String prepend() default "";

	String append() default "";

	String prependAppend() default "";

	String src() default "";

	String dst() default "";

	boolean isAsset() default false;
}