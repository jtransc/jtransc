package com.jtransc.annotation.haxe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instead of calling the default haxe build, it will execute these commands.
 *
 * It is an array representing the program/programs to execute + arguments.
 *
 * You can specify several commands separating them with the "----" string.
 *
 * Using "@file.cmd" as one element, it will load that file at that position.
 * In those file each line represents the program to execute or argument.
 *
 * Lines are trimmed and empty lines and lines started with # are ignored.
 *
 * These strings allows Minitemplates. Available variables and tags are available
 * in documentation or "haxe.kt" file in jtransc repository.
 *
 * {{ defaultBuildCommand() }} will include default haxe build command
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface HaxeCustomBuildCommandLine {
    String[] value();
}