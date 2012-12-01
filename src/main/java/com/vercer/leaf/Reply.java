package com.vercer.leaf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

public abstract class Reply
{
	private List<Header> headers;
	private Integer status = 200;
	private String type = "text/html; charset=utf-8";

	public String getContentType()
	{
		return type;
	}

	public abstract void content(OutputStream stream);

	public boolean respond(HttpServletResponse response)
	{
		header("Content-Type", getContentType());

		if (status != null)
		{
			response.setStatus(status);
		}
		
		if (headers != null)
		{
			for (Header header : headers)
			{
				if (header.value instanceof Date)
				{
					response.addDateHeader(header.name, ((Date) header.value).getTime());
				}
				else if (header.value instanceof Integer)
				{
					response.addIntHeader(header.name, (Integer) header.value);
				}
				else if (header.value instanceof String)
				{
					response.addHeader(header.name, (String) header.value);
				}
				else
				{
					throw new IllegalStateException("Header value was " + header.value);
				}
			}
		}

		try
		{
			content(response.getOutputStream());
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return true;
	}

	public Integer getStatus()
	{
		return this.status;
	}

	private static class Header
	{
		String name;
		Object value;
	}

	public static Reply withMarkup(Markup.Source source)
	{
		return new Html(source.getMarkup());
	}

	public static Reply withMarkup(Markup markup)
	{
		return new Html(markup);
	}

	public static Reply withText(String text)
	{
		return new Text(text);
	}

	public Reply header(String name, String value)
	{
		addHeader(name, value);
		return this;
	}

	public Reply header(String name, Date value)
	{
		addHeader(name, value);
		return this;
	}

	public Reply header(String name, int value)
	{
		addHeader(name, value);
		return this;
	}

	// TODO add timed caching - not enum
	public enum Cache { NEVER };
	public Reply cache(Cache cache)
	{
		if (cache == Cache.NEVER)
		{
			// http://support.microsoft.com/kb/234067
			header("Cache-Control", "no-store, no-cache, must-revalidate");
			header("Pragma", "no-cache");
			header("Expires", "-1");
		}
		return this;
	}

	public Reply status(int code)
	{
		this.status = code;
		return this;
	}
	
	public Reply type(String type)
	{
		this.type = type;
		return this;
	}

	private void addHeader(String name, Object value)
	{
		assert value != null;

		if (headers == null)
		{
			headers = new ArrayList<Reply.Header>(5);
		}
		Header header = new Header();
		header.name = name;
		header.value = value;
		headers.add(header);
	}

	public static Reply withRedirect(String location)
	{
		return new Redirect(location);
	}

	public static Reply withRedirect(URI link)
	{
		return new Redirect(link.toASCIIString());
	}

	public static class Redirect extends Reply
	{
		public Redirect(String location)
		{
			assert location != null;

			status(302);
			cache(Cache.NEVER);

			if (Leaf.get().getSettings().isSessionEncoded())
			{
				HttpServletResponse response = Leaf.get().getResponse();
				location = response.encodeRedirectURL(location);
			}
			header("Location", location);
		}

		@Override
		public void content(OutputStream stream)
		{
		}
	}

	public static class Text extends Reply
	{
		private final String text;

		public Text(String text)
		{
			this.text = text;
		}

		@Override
		public void content(OutputStream stream)
		{
			try
			{
				// TODO match up encoding with reply header
				OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
				writer.write(text);
				writer.flush();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	public static class Html extends Text
	{
		public Html(Markup markup)
		{
			super(markupToString(markup));
		}

		private static String markupToString(Markup markup)
		{
			Renderer renderer = Leaf.get().getRenderer();
			return renderer.render(markup);
		}
	}
}
