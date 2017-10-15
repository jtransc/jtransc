package com.jtransc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Forces a method to be synchronous
 * Shouldn't be necessary in future versions
 * You have to ensure that all local calls are synchronous too, or it won't work!
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface JTranscSync {
}