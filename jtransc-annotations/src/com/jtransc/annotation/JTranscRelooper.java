package com.jtransc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides enabling/disabling relooper for specific method
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface JTranscRelooper {
	boolean value() default true;
}
