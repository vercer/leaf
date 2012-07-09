package com.vercer.jet.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ ElementType.METHOD })
public @interface Handle
{
	public enum Http { POST, GET, PUT, DELETE, HEAD, ALL };
	Http[] value() default Http.ALL;
}