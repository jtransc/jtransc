package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(value = JTranscRunCommandList.class)
public @interface JTranscRunCommand {

	String target();

	String[] value();
}
