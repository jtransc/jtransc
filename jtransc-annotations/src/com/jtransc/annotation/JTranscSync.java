package com.jtransc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Forces a method to be synchronous
 *
 * Shouldn't be necessary in future versions. But allows to ensure synchronous by contract producing error at compilation time.
 *
 * You have to ensure that all local calls are synchronous too, or it won't work!
 * Don't do on virtual methods that could be overriden. Just final classes, final methods or static methods.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface JTranscSync {
}