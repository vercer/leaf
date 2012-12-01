package com.vercer.leaf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.vercer.convert.TypeConverter;
import com.vercer.leaf.Director.DefaultDirector;
import com.vercer.leaf.Dispatcher.PredicateMethod;
import com.vercer.leaf.Dispatcher.Registration;
import com.vercer.leaf.annotation.Capture;

@ImplementedBy(DefaultDirector.class)
public interface Director
{
	public Target direct(HttpServletRequest http, Throwable throwable);

	public static class DefaultDirector implements Director
	{
		private final List<Registration> registrations;

		@Inject Injector injector;
		@Inject Provider<HttpServletRequest> requests;
		@Inject TypeConverter converter;
		
		@Inject
		public DefaultDirector(List<Registration> registrations)
		{
			this.registrations = registrations;
		}

		@Override
		public Target direct(HttpServletRequest http, Throwable throwable)
		{
			String path = path(http);

			for (final Registration binding : registrations)
			{
				// show error handlers only after catching an throwing that matches
				if (throwable != null && binding.throwing == null ||
					throwable != null && !binding.throwing.isAssignableFrom(throwable.getClass()) ||
					throwable == null && binding.throwing != null)

					continue;

				if (binding.pattern == null)
				{
					throw new IllegalStateException("No pattern for " + binding.getTargetClass());
				}
				
				final Matcher matcher = binding.pattern.matcher(path);
				if (matcher.matches() && (binding.predicate == null || binding.predicate.apply(http)))
				{
					// execute an event method to get a reply
					Method method = null;
					if (binding.events != null)
					{
						for (final PredicateMethod candidate : binding.events)
						{
							if (candidate.predicate == null || candidate.predicate.apply(http))
							{
								method = candidate.method;
							}
						}
					}
					
					return new MatcherTarget(method, binding, matcher, converter, requests.get().getLocale());
				}
			}
			return null;
		}

		protected String path(HttpServletRequest http)
		{
			String path = http.getRequestURI();
			int sessionIdIndex = path.indexOf(";jsessionid=");
			if (sessionIdIndex > 0)
			{
				path = path.substring(0, sessionIdIndex);
			}
			return path;
		}
		
		private static class MatcherTarget extends Target
		{
			private final Method method;
			private final Registration binding;
			private final Matcher matcher;
			private final TypeConverter converter;
			private final Locale locale;
			
			public MatcherTarget(Method method, Registration binding, Matcher matcher, TypeConverter converter, Locale locale)
			{
				this.method = method;
				this.binding = binding;
				this.matcher = matcher;
				this.converter = converter;
				this.locale = locale;
			}
			
			@Override
			public Object receiver(Injector injector)
			{
				if (binding.receivingInstance == null)
				{
					return injector.getInstance(binding.receivingClass);
				}
				else
				{
					return binding.receivingInstance;
				}
			}
			
			@Override
			public Object parameter(Type type, Annotation annotation)
			{
				if (type.equals(MatchResult.class))
				{
					return matcher.toMatchResult();
				}
				else if (annotation.annotationType() == Capture.class)
				{
					int group = ((Capture) annotation).value();
					String text = matcher.group(group);
					return converter.convert(text, type);
				}
				return null;
			}
			
			@Override
			public Method getMethod()
			{
				return method;
			}
			
			@Override
			public Locale getLocale()
			{
				return locale;
			}
		}
	}
	
}
