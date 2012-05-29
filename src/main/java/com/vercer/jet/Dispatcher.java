package com.vercer.jet;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.common.base.Predicate;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.vercer.convert.TypeConverter;
import com.vercer.generics.Generics;
import com.vercer.jet.Markup.Source;

@Singleton @Log
public class Dispatcher
{
	private static final Object NOT_SET = new Object();

	@Inject Injector injector;
	@Inject TypeConverter converter;
	@Inject Provider<HttpServletRequest> requests;

	private final List<Registration> bindings;
	private final ParametersProxyHandler handler = new ParametersProxyHandler();

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	public @interface Pull
	{
		String value() default "";
		String fallback() default "";
	}

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	public @interface Push
	{
	}

	@Retention(RUNTIME)
	@Target({ ElementType.FIELD, ElementType.PARAMETER })
	public @interface Capture
	{
		int value() default 1;
	}

	@Retention(RUNTIME)
	@Target({ ElementType.METHOD })
	public @interface Default
	{
		String[] value();
	}

	public static class Registration
	{
		Pattern pattern;
		Predicate<HttpServletRequest> predicate;
		Class<?> receivingClass;
		Object receivingInstance;
		Map<String, Method> events;
		Class<? extends Throwable> throwing;
	}

	@Inject
	public Dispatcher(List<Registration> bindings)
	{
		this.bindings = bindings;
	}

	public boolean dispatch(HttpServletRequest request, HttpServletResponse response) throws Throwable
	{
		try
		{
			// try each binding in order until one matches
			return handle(request, response, null);
		}
		catch (Throwable t)
		{
			// unwrap exceptions thrown while creating replies
			if (t instanceof ProvisionException)
			{
				t = ((ProvisionException) t).getCause();
			}
			else if (t instanceof InvocationTargetException)
			{
				t = ((InvocationTargetException) t).getTargetException();
			}

			try
			{
				// make the cause available to error page
				Jet.setThrowable(t);

				// try to show an error page
				if(!handle(request, response, t))
				{
					throw t;
				}
				else
				{
					return true;
				}
			}
			finally
			{
				Jet.setThrowable(null);
			}
		}
	}

	public boolean handle(HttpServletRequest request, HttpServletResponse httpServletResponse, Throwable throwable) throws Throwable
	{
		for (Registration binding : bindings)
		{
			// show error handlers only after catching an throwing that matches
			if (throwable != null && binding.throwing == null ||
				throwable != null && !binding.throwing.isAssignableFrom(throwable.getClass()) ||
				throwable == null && binding.throwing != null)

				continue;

			String path = request.getRequestURI();
			int sessionIdIndex = path.indexOf(";jsessionid=");
			if (sessionIdIndex > 0)
			{
				path = path.substring(0, sessionIdIndex);
			}

			Matcher matcher = binding.pattern.matcher(path);
			if (matcher.matches() && (binding.predicate == null || binding.predicate.apply(request)))
			{
				Object target;
				if (binding.receivingInstance == null)
				{
					target = injector.getInstance(binding.receivingClass);
				}
				else
				{
					target = binding.receivingInstance;
				}

				Object result = null;
				if (binding.events != null)
				{
					Method method = binding.events.get(request.getMethod().toUpperCase());
					if (method != null)
					{
						result = call(target, method, matcher);
					}
				}

				Reply reply;
				if (result instanceof Reply)
				{
					reply = (Reply) result;
				}
				else
				{
					if (result == null)
					{
						if (target instanceof Markup.Source)
						{
							// hard code markup response to save some cycles
							reply = Reply.withMarkup((Source) target);
						}
						else
						{
							reply = converter.convert(target, Reply.class);
						}
					}
					else
					{
						reply = converter.convert(result, Reply.class);
					}

					if (reply == null)
					{
						throw new IllegalArgumentException("Could not create response from " + target);
					}

					// unless a reply explicitly sets the status use 500 for errors
					if (throwable != null && reply.getStatus() == null)
					{
						reply.status(500);
					}
				}

				reply.respond(httpServletResponse);

				return true;
			}
		}

		return false;
	}

	private Object call(Object receiver, Method method, Matcher matcher) throws Throwable
	{
		Type[] types = method.getGenericParameterTypes();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Object[] parameters = new Object[types.length];
		for (int i = 0; i < types.length; i++)
		{
			Type type = types[i];
			Class<?> erased = Generics.erase(type);
			Annotation[] annotations = parameterAnnotations[i];

			Object parameter = NOT_SET;
			for (Annotation annotation : annotations)
			{
				// first look for @Pull annotations
				if (annotation.annotationType() == Pull.class)
				{
					String name = ((Pull) annotation).value();
					
					// no name means we expect an bean interface 
					if (name.isEmpty())
					{
						// make a proxy for the interface
						parameter = Proxy.newProxyInstance(
								getClass().getClassLoader(),
								new Class[] {erased},
								handler);
					}
					else
					{
						String[] values = requests.get().getParameterValues(name);
						parameter = parametersToArgument(type, values);
					}
					break;
				}
				else if (annotation.annotationType() == Capture.class)
				{
					int group = ((Capture) annotation).value();
					String value = matcher.group(group);
					parameter = converter.convert(value, String.class, type);
				}
				else
				{
					// allow passing in special bound objects like request parameters
					if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class))
					{
						parameter = injector.getInstance(Key.get(type, annotation));
					}
					break;
				}
			}

			// no annotation so just inject it normally
			if (parameter == NOT_SET)
			{
				if (type.equals(MatchResult.class))
				{
					parameter = matcher.toMatchResult();
				}
				else
				{
					parameter = injector.getInstance(Key.get(type));
				}
			}

			parameters[i] = parameter;
		}
		return method.invoke(receiver, parameters);
	}

	private Object parametersToArgument(Type type, String[] values)
	{
		// if we have a single value treat it as a String (not array)
		Class<?> erased = Generics.erase(type);
		if (values != null && (values.length > 1 || erased.isArray() || Collection.class.isAssignableFrom(erased)))
		{
			// we have more than one value so convert a String[]
			return converter.convert(values, String[].class, type);
		}
		else
		{
			// convert null or a single string - can be converted to list
			String value;
			if (values == null)
			{
				value = null;
			}
			else
			{
				value = values[0];
			}
			return converter.convert(value, String.class, type);
		}
	}

	public class ParametersProxyHandler implements InvocationHandler
	{
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			if (args != null)
			{
				throw new IllegalStateException("Cannot pass args to request parameter proxy");
			}

			String name = method.getName();
			if (name.startsWith("get"))
			{
				name = name.substring(3);
			}
			else if(name.startsWith("is"))
			{
				name = name.substring(2);
			}
			String[] values = requests.get().getParameterValues(name);
			if (values == null)
			{
				Default def = method.getAnnotation(Default.class);
				if (def != null)
				{
					values = def.value();
				}
			}

			return parametersToArgument(method.getReturnType(), values);
		}
	}
}
