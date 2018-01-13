package com.jtransc.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
@Repeatable(value = JTranscTargetClassImplList.class)
public @interface JTranscTargetClassImpl {
	String target();

	String cond() default "";

	Class<?> implementation();
}