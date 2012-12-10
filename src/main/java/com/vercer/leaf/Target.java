package com.vercer.leaf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import com.google.inject.Injector;

// TODO make an abstract class that contains all basic behaviour
public abstract class Target
{
	public abstract Object receiver(Injector injector);
	
	public abstract Object parameter(Type type, Annotation annotation);
	
	public abstract Method getMethod();

	public abstract Locale getLocale();
}
