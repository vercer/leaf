package com.vercer.jet;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

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
import com.vercer.jet.Dispatcher.Registration;
import com.vercer.jet.Settings.Builder;
import com.vercer.jet.convert.BooleanToTransformer;
import com.vercer.jet.convert.ObjectToLabel;
import com.vercer.jet.convert.ObjectToResponse;
import com.vercer.jet.convert.StringToLabel;
import com.vercer.jet.convert.StringToReply;
import com.vercer.jet.convert.UriToAnchor;
import com.vercer.jet.convert.UrlToAnchor;
import com.vercer.jet.transform.Container;
import com.vercer.jet.transform.Transformer;

public abstract class JetModule extends ServletModule
{
	public enum HttpMethod { POST, GET, PUT, DELETE, HEAD, ALL };

	@Retention(RUNTIME)
	@Target({ ElementType.METHOD })
	public @interface Handle
	{
		HttpMethod[] value() default HttpMethod.ALL;
	}

	@Retention(RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Match
	{
		String value();
	}
	@Retention(RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Catch
	{
		Class<? extends Throwable> value();
	}

	private Settings settings;
	private Builder builder = Settings.builder();

	public class RegistrationBinder
	{
		private final Registration registration;
		public RegistrationBinder(Registration registration)
		{
			this.registration = registration;
		}

		public RegistrationBinder at(String regex)
		{
			registration.pattern = Pattern.compile(regex);
			return this;
		}

		public EventBinder on(HttpMethod method)
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
			private final HttpMethod http;

			public EventBinder(HttpMethod http)
			{
				this.http = http;
			}

			public RegistrationBinder call(String event)
			{
				Method[] methods = registration.receivingClass.getClass().getDeclaredMethods();
				for (Method method : methods)
				{
					if (method.getName().equals(event))
					{
						registration.events.put(http.name(), method);
						return RegistrationBinder.this;
					}
				}
				throw new IllegalArgumentException("Could not find method " + event);
			}
		}
	}


	private List<Dispatcher.Registration> registrations = new ArrayList<Dispatcher.Registration>();
	private Multibinder<Converter<?, ?>> converters;
	private Multibinder<Transformer> transformers;

	protected abstract void configure(Builder builder);

	@Override
	public final void configureServlets()
	{
		requestStaticInjection(Jet.class);
		install(new ServletModule()
		{
			@Override
			protected void configureServlets()
			{
				filter("/*").through(JetFilter.class);
			}
		});
		bind(new TypeLiteral<List<Registration>>() {}).toInstance(registrations);
		requestStaticInjection(Container.class);
		bind(TypeConverter.class).to(GuiceTypeConverter.class).in(Scopes.SINGLETON);
	    converters = Multibinder.newSetBinder(binder(), new TypeLiteral<Converter<?, ?>>() {});
	    transformers = Multibinder.newSetBinder(binder(), new TypeLiteral<Transformer>() {});
		configure(builder);
		bindDefaultConverters(converters);
		settings = builder.build();
		bind(Settings.class).toInstance(settings);
		
		builder = null;
	}
	
	protected void use(Configuration configuration)
	{
		configuration.configure(builder, binder());
	}

	public static class GuiceTypeConverter extends CombinedTypeConverter
	{
		@com.google.inject.Inject
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
		converters.addBinding().toInstance(new ObjectToLabel());
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
		return Jet.get().getThrowable();
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

	public RegistrationBinder reply(Class<?> view)
	{
		Registration registration = new Registration();
		registrations.add(registration);
		RegistrationBinder binding = new RegistrationBinder(registration);

		registration.receivingClass = view;

		reflect(view, registration);

		return binding;
	}

	public RegistrationBinder reply(Object view)
	{
		Registration registration = new Registration();
		registrations.add(registration);
		RegistrationBinder binding = new RegistrationBinder(registration);

		registration.receivingInstance = view;

		reflect(view.getClass(), registration);

		return binding;
	}

	private void reflect(Class<?> view, Registration registration)
	{
		Match at = view.getAnnotation(Match.class);

		if (at != null)
		{
			registration.pattern = Pattern.compile(at.value());
		}

		Catch katch = view.getAnnotation(Catch.class);
		if (katch != null)
		{
			registration.throwing = katch.value();
		}

		Method[] methods = view.getDeclaredMethods();
		for (Method method : methods)
		{
			Handle on = method.getAnnotation(Handle.class);
			if (on != null)
			{
				if (registration.events == null)
				{
					registration.events = new HashMap<String, Method>(2);
				}
				HttpMethod[] values = on.value();
				if (values[0] == HttpMethod.ALL)
				{
					values = HttpMethod.values();
				}

				for (HttpMethod value : values)
				{
					registration.events.put(value.name(), method);
				}
			}
		}
	}

	public final void global(Transformer transformer)
	{
		transformers.addBinding().toInstance(transformer);
	}

	public final ScopedBindingBuilder transformer(Class<? extends Transformer> transformer)
	{
		return transformers.addBinding().to(transformer);
	}

	@Provides
	public Renderer getRenderer(Settings settings)
	{
		return new Renderer(settings.isRemoveSpecialTags(), settings.isRemoveSpecialAttributes());
	}
}
