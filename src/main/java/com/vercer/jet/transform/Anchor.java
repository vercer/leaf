package com.vercer.jet.transform;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.vercer.jet.Jet;
import com.vercer.jet.Markup;
import com.vercer.jet.Markup.Builder;

public class Anchor extends Container<URI>
{
	private static List<String> stickies;
	private static ThreadLocal<Boolean> threadAbsolute = new ThreadLocal<Boolean>();

	private final Provider<String> title;
	private Multimap<String, Object> parameters;
	private boolean absolute;

	public Anchor(URI uri)
	{
		this(uri, null);
	}

	public Anchor(String href)
	{
		this(URI.create(href));
	}

	public Anchor(URI href, String title)
	{
		super(href);
		this.title = title == null ? null : Providers.of(title);
	}

	public Anchor(Provider<URI> href, Provider<String> title)
	{
		super(href);
		this.title = title;
	}

	public Anchor(String href, String title)
	{
		this(URI.create(href), title);
	}

	public Markup transformAnchor(Markup markup)
	{
		return super.transformComponent(markup);
	}

	public static void setThreadAbsolute(boolean absolute)
	{
		threadAbsolute.set(absolute);
	}

	@Override
	protected final Markup transformComponent(Markup markup)
	{
		markup = transformAnchor(markup);

		String uri = get().toString();
		StringBuilder builder = null;

		if (stickies != null && !stickies.isEmpty())
		{
			builder = new StringBuilder(uri);
			HttpServletRequest request = Jet.get().getRequest();
			for (String  parameter : stickies)
			{
				String value = request.getParameter(parameter);
				addNotNullParameter(builder, parameter, value);
			}
		}

		if (parameters != null)
		{
			if (builder == null)
			{
				builder = new StringBuilder(uri);
			}

			for (String  parameter : parameters.keySet())
			{
				Collection<Object> values = parameters.get(parameter);
				for (Object value : values)
				{
					addNotNullParameter(builder, parameter, value);
				}
			}
		}

		if (builder != null)
		{
			uri = builder.toString();
		}

		// add session id if enabled
		if (Jet.get().getSettings().isSessionEncoded())
		{
			uri = Jet.get().getResponse().encodeURL(uri);
		}

		if (absolute || threadAbsolute.get() == Boolean.TRUE)
		{
			HttpServletRequest request = Jet.get().getRequest();
			URI current = URI.create(request.getRequestURL().toString());
			URI resolved = current.resolve(uri);
			uri = resolved.toString();
		}

		// add href and title attributes
		Builder result = Markup.builder(markup).attribute("href", uri);
		if (title != null)
		{
			result.attribute("title", title.get());
		}

		// transform the contained markup
		return result.build();
	}

	private void addNotNullParameter(StringBuilder builder, String name, Object value)
	{
		if (value != null)
		{
			if (builder.indexOf("?") > 0)
			{
				builder.append('&');
			}
			else
			{
				builder.append('?');
			}
			builder.append(name);
			builder.append('=');
			try
			{
				builder.append(URLEncoder.encode(value.toString(), "UTF-8"));
			}
			catch (UnsupportedEncodingException e)
			{
				throw new AssertionError(e);
			}
		}
	}


	/**
	 * Adds a sticky parameter which, if present in the current request, is
	 * added to the link as a query parameter.
	 *
	 * Not thread safe but should be called from single threaded start-up code
	 *
	 * @param parameter The parameter name to pass on
	 */
	public static void sticky(String parameter)
	{
		if (stickies == null)
		{
			stickies = new ArrayList<String>();
		}
		stickies.add(parameter);
	}

	public Anchor add(String name, Object value)
	{
		if (parameters == null)
		{
			parameters = ArrayListMultimap.create();
		}
		parameters.put(name, value);
		return this;
	}

	public Anchor remove(String name)
	{
		parameters.removeAll(name);
		return this;
	}

	public Anchor absolute()
	{
		this.absolute = true;
		return this;
	}
}
