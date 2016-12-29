package com.jtransc.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Repeatable(value = JTranscRegisterCommandList.class)
public @interface JTranscRegisterCommand {
	String target();

	String name();

	String command();

	String[] check();

	String[] getFolderCmd() default "";

	String[] install();
}
