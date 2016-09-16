package com.jtransc.annotation.haxe;

import java.lang.annotation.*;

/**
 * Copies files from available Java resources to the assets folder in the output.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface HaxeAddAssets {
	String[] value();
}