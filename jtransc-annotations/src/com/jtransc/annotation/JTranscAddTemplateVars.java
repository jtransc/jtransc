package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(value = JTranscAddTemplateVarsList.class)
public @interface JTranscAddTemplateVars {
	String variable();

	String[] list();

	String target() default "";
}