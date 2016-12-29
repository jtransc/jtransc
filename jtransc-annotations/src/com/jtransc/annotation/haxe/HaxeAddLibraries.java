package com.jtransc.annotation.haxe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds haxelib haxe libraries to the project so you can use them.
 *
 * For example: "lime:2.9.1" would include version 2.9.1 of the library lime.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface HaxeAddLibraries {
	String[] value();
}