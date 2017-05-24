package com.jtransc.annotation;

import java.lang.annotation.*;

/**
 * Add members to the generated haxe classes.
 * <p>
 * Here you can add fields and methods.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(value = JTranscAddMembersList.class)
public @interface JTranscAddMembers {
	String target();

	String[] value();

	String cond() default "";
}