package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface JTranscCallSiteBodyList {
	JTranscCallSiteBody[] value() default {};
}
