package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@Repeatable(value = JTranscAddLibrariesList.class)
public @interface JTranscAddLibraries {
	String target();
	String cond() default "";
	String[] value();
}