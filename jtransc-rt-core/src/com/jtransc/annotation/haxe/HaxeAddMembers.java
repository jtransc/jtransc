package com.jtransc.annotation.haxe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add members to the generated haxe classes.
 *
 * Here you can add fields and methods.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface HaxeAddMembers {
    String[] value();
}