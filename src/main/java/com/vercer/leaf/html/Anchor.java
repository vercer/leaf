package com.vercer.leaf.html;

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
import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;
import com.vercer.leaf.exchange.Container;

public class Anchor extends Container
{
	private static List<String> stickies;
	private static ThreadLocal<Boolean> threadAbsolute = new ThreadLocal<Boolean>();

	private final Provider<String> title;
	private Multimap<String, Object> parameters;
	private boolean absolute;
	private String frame;
	private URI uri;

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
		this(href, title, null);
	}
	
	public Anchor(URI href, String title, String frame)
	{
		this.uri = href;
		this.frame = frame;
		this.title = title == null ? null : Providers.of(title);
	}

	public Anchor(String href, String title)
	{
		this(URI.create(href), title);
	}

	public Markup transformAnchor(Markup markup)
	{
		return super.exchangeComponent(markup);
	}

	public static void setThreadAbsolute(boolean absolute)
	{
		threadAbsolute.set(absolute);
	}

	@Override
	protected final Markup exchangeComponent(Markup markup)
	{
		markup = transformAnchor(markup);

		String uri = toUri().toASCIIString();

		// add href and title attributes
		Builder result = Markup.builder(markup).attribute("href", uri);
		if (title != null)
		{
			result.attribute("title", title.get());
		}
		
		if (frame != null)
		{
			result.attribute("target", frame);
		}

		// transform the contained markup
		return result.build();
	}

	public URI toUri()
	{
		StringBuilder builder = null;

		if (stickies != null && !stickies.isEmpty())
		{
			builder = new StringBuilder(uri.toASCIIString());
			HttpServletRequest request = Leaf.get().getRequest();
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
				builder = new StringBuilder(uri.toASCIIString());
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

		String string;
		if (builder != null)
		{
			string = builder.toString();
		}
		else
		{
			string = uri.toASCIIString();
		}

		// add session id if enabled
		if (Leaf.get().getSettings().isSessionEncoded())
		{
			string = Leaf.get().getResponse().encodeURL(string);
		}

		if (absolute || threadAbsolute.get() == Boolean.TRUE)
		{
			HttpServletRequest request = Leaf.get().getRequest();
			URI current = URI.create(request.getRequestURL().toString());
			URI resolved = current.resolve(string);
			string = resolved.toString();
		}
		
		return URI.create(string);
	}

	public static void addNotNullParameter(StringBuilder builder, String name, Object value)
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
