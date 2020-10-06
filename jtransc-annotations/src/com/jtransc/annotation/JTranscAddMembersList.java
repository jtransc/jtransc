package com.jtransc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add members to the generated classes.
 * <p>
 * Here you can add fields and methods.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface JTranscAddMembersList {
	JTranscAddMembers[] value() default {};
}