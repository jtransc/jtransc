package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface JTranscRunCommandList {
	JTranscRunCommand[] value() default {};
}

