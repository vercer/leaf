package com.vercer.leaf;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Predicate;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.vercer.convert.TypeConverter;
import com.vercer.generics.Generics;
import com.vercer.leaf.Markup.Source;
import com.vercer.leaf.annotation.Cookie;
import com.vercer.leaf.annotation.Parameter;

@Singleton
public class Dispatcher
{
	private static final Object NOT_SET = new Object();

	@Inject Set<Director> directors;
	@Inject Injector injector;
	@Inject TypeConverter converter;

	public static class Registration
	{
		Pattern pattern;
		Predicate<HttpServletRequest> predicate;
		Class<?> receivingClass;
		Object receivingInstance;
		List<PredicateMethod> events;
		Class<? extends Throwable> throwing;
		
		public Class<?> getTargetClass()
		{
			if (receivingClass == null)
			{
				return receivingInstance.getClass();
			}
			else
			{
				return receivingClass;
			}
		}
	}
	
	public static class PredicateMethod
	{
		public PredicateMethod(Predicate<HttpServletRequest> predicate, Method method)
		{
			this.predicate = predicate;
			this.method = method;
		}
		Predicate<HttpServletRequest> predicate;
		Method method;
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
				Leaf.setThrowable(t);

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
				Leaf.setThrowable(null);
			}
		}
	}

	public boolean handle(HttpServletRequest hreq, HttpServletResponse hres, Throwable throwable) throws Throwable
	{
		for (Director director : directors)
		{
			Target request = director.direct(hreq, throwable);
			if (request != null)
			{
				LeafModule.setCurrentRequest(request);
				try
				{
					Object result = call(request, hreq);
					Reply response = response(result, hres);
					response.respond(hres);
					return true;
				}
				finally
				{
					LeafModule.setCurrentRequest(null);
				}
			}
		}
		return false;
	}
	
	private Reply response(Object result, HttpServletResponse hres)
	{
		// convert the result object to a reply
		Reply reply;
		if (result instanceof Reply)
		{
			reply = (Reply) result;
		}
		else
		{
			// if no result then use the target itself as result
			if (result instanceof Markup.Source)
			{
				// hard code markup response to save some cycles
				reply = Reply.withMarkup((Source) result);
			}
			else
			{
				// use the converter for everything else so we can override
				reply = converter.convert(result, Reply.class);
			}

			if (reply == null)
			{
				throw new IllegalArgumentException("Could not create response from " + result);
			}
		}
		return reply;
	}

	private Object call(Target target, HttpServletRequest request) throws Throwable
	{
		// create the actual target page
		Object receiver = target.receiver(injector);
		
		if (target.getMethod() == null)
		{
			return receiver;
		}
		
		Type[] types = target.getMethod().getGenericParameterTypes();
		Annotation[][] parameterAnnotations = target.getMethod().getParameterAnnotations();
		Object[] parameters = new Object[types.length];
		for (int i = 0; i < types.length; i++)
		{
			Type type = types[i];
			Annotation[] annotations = parameterAnnotations[i];

			Object parameter = NOT_SET;
			for (Annotation annotation : annotations)
			{
				if (annotation.annotationType() == Parameter.class)
				{
					String name = ((Parameter) annotation).value();
					String[] values = request.getParameterValues(name);
					parameter = parametersToArgument(type, values);
				}
				else if (annotation.annotationType() == Cookie.class)
				{
					String name = ((Cookie) annotation).value();
					javax.servlet.http.Cookie[] cookies = request.getCookies();
					for (javax.servlet.http.Cookie cookie : cookies)
					{
						if (cookie.getName().equals(name))
						{
							parameter = converter.convert(cookie, type);
							break;
						}
					}
				}
				else if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class))
				{
					// allow passing in special bound objects like request parameters
					parameter = injector.getInstance(Key.get(type, annotation));
				}
				else
				{
					// ask the request for the parameter
					parameter = target.parameter(type, annotation);
				}
				if (parameter != null) break;
			}

			// no annotation so just inject it normally
			if (parameter == NOT_SET)
			{
				parameter = injector.getInstance(Key.get(type));
			}

			parameters[i] = parameter;
		}
		
		// call a method on it to 
		Object result = target.getMethod().invoke(receiver, parameters);
		
		if (result == null)
		{
			return receiver;
		}
		else
		{
			return result;
		}
	}
	
	protected Object parametersToArgument(Type type, String[] values)
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
}
