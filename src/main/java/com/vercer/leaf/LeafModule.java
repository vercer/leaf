package com.vercer.leaf;


import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.vercer.convert.ArrayToList;
import com.vercer.convert.CombinedTypeConverter;
import com.vercer.convert.Converter;
import com.vercer.convert.DateConverters;
import com.vercer.convert.NumberConverters;
import com.vercer.convert.ObjectToString;
import com.vercer.convert.SingletonListConverters;
import com.vercer.convert.StringArrayToString;
import com.vercer.convert.StringToPrimitive;
import com.vercer.convert.ThrowableToString;
import com.vercer.convert.TypeConverter;
import com.vercer.leaf.Dispatcher.PredicateMethod;
import com.vercer.leaf.Dispatcher.Registration;
import com.vercer.leaf.LeafModule.RegistrationBinder.EventBinder;
import com.vercer.leaf.Settings.SettingsBuilder;
import com.vercer.leaf.annotation.Catch;
import com.vercer.leaf.annotation.Discriminator;
import com.vercer.leaf.annotation.Handle;
import com.vercer.leaf.annotation.Handle.Http;
import com.vercer.leaf.annotation.Match;
import com.vercer.leaf.convert.BooleanToTransformer;
import com.vercer.leaf.convert.ObjectToLabel;
import com.vercer.leaf.convert.ObjectToResponse;
import com.vercer.leaf.convert.StringToLabel;
import com.vercer.leaf.convert.StringToReply;
import com.vercer.leaf.convert.UriToAnchor;
import com.vercer.leaf.convert.UrlToAnchor;
import com.vercer.leaf.exchange.Container;
import com.vercer.leaf.exchange.Exchanger;

public abstract class LeafModule extends ServletModule
{
	private Settings settings;
	private SettingsBuilder builder = Settings.builder();
	private static ThreadLocal<Target> requests = new ThreadLocal<Target>();
	
	public class RegistrationBinder
	{
		private final Registration registration;
		public RegistrationBinder(Registration registration)
		{
			this.registration = registration;
		}

		public RegistrationBinder match(String regex)
		{
			registration.pattern = Pattern.compile(regex);
			return this;
		}

		public EventBinder on(Http method)
		{
			return new EventBinder(method);
		}

		public RegistrationBinder throwing(Class<? extends Throwable> throwing)
		{
			registration.throwing = throwing;
			return this;
		}

		public class EventBinder
		{
			private final Http http;
			private Predicate<HttpServletRequest> predicate;

			public EventBinder(Http http)
			{
				this.http = http;
			}

			public EventBinder parameter(String name, String value)
			{
				andPredicate(new ParameterPredicate(name, value));
				return this;
			}
			
			private void andPredicate(Predicate<HttpServletRequest> predicate)
			{
				if (this.predicate == null)
				{
					this.predicate = predicate;
				}
				else
				{
					this.predicate = Predicates.and(this.predicate, predicate);
				}
			}

			public RegistrationBinder call(String event)
			{
				Method[] methods = registration.getTargetClass().getDeclaredMethods();
				for (Method method : methods)
				{
					if (method.getName().equals(event))
					{
						if (registration.events == null)
						{
							registration.events = new ArrayList<Dispatcher.PredicateMethod>(2);
						}
						
						if (http != Http.ALL)
						{
							andPredicate(new HttpMethodPredicate(http));
						}
						
						registration.events.add(new PredicateMethod(predicate, method));
						return RegistrationBinder.this;
					}
				}
				throw new IllegalArgumentException("Could not find method " + event);
			}
		}
	}
	
	private List<Dispatcher.Registration> registrations = new ArrayList<Dispatcher.Registration>();
	private Multibinder<Converter<?, ?>> converters;
	private Multibinder<Exchanger> transformers;
	private Multibinder<Director> directors;

	protected abstract void configure(SettingsBuilder builder);

	@Override
	public final void configureServlets()
	{
		bind(new TypeLiteral<List<Registration>>() {}).toInstance(registrations);
		requestStaticInjection(Container.class);
		bind(TypeConverter.class).to(GuiceTypeConverter.class).in(Scopes.SINGLETON);
	    converters = Multibinder.newSetBinder(binder(), new TypeLiteral<Converter<?, ?>>() {});
	    transformers = Multibinder.newSetBinder(binder(), new TypeLiteral<Exchanger>() {});
	    directors = Multibinder.newSetBinder(binder(), new TypeLiteral<Director>() {});
	    directors.addBinding().to(Director.class);
		configure(builder);
		bindDefaultConverters(converters);
		settings = builder.build();
		builder.addAllowedLocale(settings.getDefaultLocale());
		bind(Settings.class).toInstance(settings);
		builder = null;
		requestStaticInjection(Leaf.class);

		filter("/*").through(LeafFilter.class);
	}
	
	protected void use(Configuration configuration)
	{
		configuration.configure(builder, binder());
	}

	/**
	 *	Injectable type converter
	 */
	public static class GuiceTypeConverter extends CombinedTypeConverter
	{
		@Override
		public <T> T convert(Object input, Type source, Type target)
		{
			if (input == null && source == String.class)
			{
				input = "";
			}
			return super.convert(input, source, target);
		}
		
		@Inject
		public void registerConverters(Set<Converter<?, ?>> converters)
		{
			super.registerAll(converters);
		}
	}

	protected void bindDefaultConverters(Multibinder<Converter<?, ?>> converters)
	{
		converters.addBinding().toInstance(new ObjectToString());
		converters(converters, new NumberConverters());
		converters(converters, new DateConverters());
		converters(converters, new SingletonListConverters());
		converters(converters, new StringToPrimitive());
		converters.addBinding().toInstance(new StringArrayToString());
		converters.addBinding().to(ArrayToList.class);
		converters.addBinding().toInstance(new ObjectToString());
		converters.addBinding().toInstance(new ThrowableToString());
		converters.addBinding().toInstance(new StringToLabel());
		converters.addBinding().toInstance(new BooleanToTransformer());
		converters.addBinding().toInstance(new UriToAnchor());
		converters.addBinding().toInstance(new UrlToAnchor());
		converters.addBinding().toInstance(new StringToReply());
		converters.addBinding().to(ObjectToResponse.class).in(Scopes.SINGLETON);
		converters.addBinding().to(ObjectToLabel.class).in(Scopes.SINGLETON);
	}

	@Provides
	public Throwable getCurrentProblem()
	{
		return Leaf.get().getThrowable();
	}

	protected void converters(Multibinder<Converter<?, ?>> converters, Iterable<Converter<?, ?>> aggregate)
	{
		for (Converter<?, ?> converter : aggregate)
		{
			converters.addBinding().toInstance(converter);
		}
	}

	public final void converter(Converter<?, ?> converter)
	{
		converters.addBinding().toInstance(converter);
	}

	public final ScopedBindingBuilder converter(Class<? extends Converter<?, ?>> clazz)
	{
		return converters.addBinding().to(clazz);
	}

	public final void director(Director director)
	{
		directors.addBinding().toInstance(director);
	}
	
	public final void director(Class<? extends Director> director)
	{
		directors.addBinding().to(director);
	}
	
	public RegistrationBinder reply(Class<?> view)
	{
		Registration registration = new Registration();
		registrations.add(registration);
		RegistrationBinder binding = new RegistrationBinder(registration);

		registration.receivingClass = view;

		reflect(view, binding);

		return binding;
	}

	public RegistrationBinder reply(Object view)
	{
		Registration registration = new Registration();
		registrations.add(registration);
		RegistrationBinder binding = new RegistrationBinder(registration);
		registration.receivingInstance = view;
		reflect(view.getClass(), binding);

		return binding;
	}
	
	private void reflect(Class<?> view, RegistrationBinder binder)
	{
		// check class annotations
		Match at = view.getAnnotation(Match.class);
		if (at != null)
		{
			binder.match(at.value());
		}

		Catch katch = view.getAnnotation(Catch.class);
		if (katch != null)
		{
			binder.throwing(katch.value());
		}
		
		// check method annotations
		// TODO check superclass fields and methods
		Method[] methods = view.getDeclaredMethods();
		for (Method method : methods)
		{
			Handle on = method.getAnnotation(Handle.class);
			if (on != null)
			{
				Discriminator parameter = method.getAnnotation(Discriminator.class);
				
				Http[] values = on.value();
				for (Http value : values)
				{
					EventBinder eb = binder.on(value);
					if (parameter != null)
					{
						eb.parameter(parameter.name(), parameter.value());
					}
					eb.call(method.getName());
				}
			}
		}
	}

	public final void global(Exchanger transformer)
	{
		transformers.addBinding().toInstance(transformer);
	}

	public final ScopedBindingBuilder transformer(Class<? extends Exchanger> transformer)
	{
		return transformers.addBinding().to(transformer);
	}

	@Provides
	public Renderer getRenderer(Settings settings)
	{
		return new Renderer(settings.isRemoveSpecialTags(), settings.isRemoveSpecialAttributes());
	}

	public static void setCurrentRequest(Target request)
	{
		requests.set(request);
	}
	
	@Provides
	public Target getRequest()
	{
		return requests.get();
	}
	
	@Provides
	public Locale getLocale()
	{
		return getRequest().getLocale();
	}
}
