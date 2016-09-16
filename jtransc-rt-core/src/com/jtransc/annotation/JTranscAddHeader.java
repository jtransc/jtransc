package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@Repeatable(value = JTranscAddHeaderList.class)
public @interface JTranscAddHeader {
	String target();
	String cond() default "";
	String[] value();
}