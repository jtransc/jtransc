package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Repeatable(value = JTranscCallSiteBodyList.class)
public @interface JTranscCallSiteBody {
	String target();

	String[] value();
}