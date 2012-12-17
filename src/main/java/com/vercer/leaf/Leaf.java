package com.vercer.leaf;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.vercer.convert.TypeConverter;

/**
 * Singleton holding instances that are commonly needed and would
 * be expensive to inject into every client. They are injected once
 * here and then referenced statically providing a global cache
 * after guice wires everything together.
 *
 * @author John Patterson (john@vercer.com)
 *
 */
/**
 * @author John Patterson (john@vercer.com)
 *
 */
@Singleton
public class Leaf
{
	@Inject
	private static Leaf instance;

	@Inject @Getter
	Injector injector;

	@Inject @Getter
	TypeConverter converter;
	
	@Inject
	Provider<HttpServletRequest> requests;

	@Inject
	Provider<HttpServletResponse> responses;

	@Inject
	Provider<Target> targets;

	@Inject @Getter
	Settings settings;

	@Inject @Getter
	Renderer renderer;

	@Inject @Getter
	Dispatcher dispatcher;

	public static Leaf get()
	{
		if (instance == null)
		{
			throw new IllegalStateException("Jet singleton has not been injected");
		}
		return instance;
	}

	/**
	 * For testing
	 */
	static void set(Leaf jet)
	{
		instance = jet;
	}

	public HttpServletRequest getRequest()
	{
		return requests.get();
	}

	public HttpServletResponse getResponse()
	{
		return responses.get();
	}
	
	public Target getTarget()
	{
		return targets.get();
	}

	private static final ThreadLocal<Throwable> throwableLocal = new ThreadLocal<Throwable>();

	static void setThrowable(Throwable throwable)
	{
		throwableLocal.set(throwable);
	}

	@Provides
	public Throwable getThrowable()
	{
		return throwableLocal.get();
	}

	public ServletContext getServletContext()
	{
		return injector.getInstance(ServletContext.class);
	}
}
