package com.jtransc.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
public @interface JTranscRunCommandList {
	JTranscRunCommand[] value() default {};
}

