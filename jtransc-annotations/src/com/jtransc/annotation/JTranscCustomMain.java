package com.jtransc.annotation;

import java.lang.annotation.*;


@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(value = JTranscCustomMainList.class)
public @interface JTranscCustomMain {
	String target();

	String value();
}
