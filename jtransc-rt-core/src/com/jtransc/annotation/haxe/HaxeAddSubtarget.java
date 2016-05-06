package com.jtransc.annotation.haxe;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(HaxeAddSubtargetList.class)
public @interface HaxeAddSubtarget {
	String name();
	String cmdSwitch() default "-bin";
	String[] alias() default {};
	boolean singleFile() default true;
	String interpreter() default "echo";
	String extension() default "bin";
	String interpreterSuffix() default "";
	String extra() default "";

	//val switch: String, val singleFile: Boolean, val interpreter: String? = null, val extension: String = "bin", val interpreterSuffix: String = ""
}