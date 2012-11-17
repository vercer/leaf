package com.vercer.leaf.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.io.CharStreams;
import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;
import com.vercer.leaf.exchange.Exchanger;

public class InlineSource implements Exchanger
{
	private final boolean cache;
	private String content;
	private String attribute;
	private String rename;

	public InlineSource()
	{
		this(true, "src");
	}

	public InlineSource(boolean cache)
	{
		this(cache, "src");
	}

	public InlineSource(boolean cache, String attribute)
	{
		this.cache = cache;
		this.attribute = attribute;
	}

	public InlineSource rename(String rename)
	{
		this.rename = rename;
		return this;
	}

	@Override
	public Markup exchange(Markup markup)
	{
		try
		{
			if (!cache || content == null)
			{
				String source = markup.getAttributes().get(attribute);
				InputStream stream = Leaf.get().getServletContext().getResourceAsStream(source);
				content = CharStreams.toString(new InputStreamReader(stream));
			}

			Builder builder = Markup.builder(markup).abandon().attribute(attribute, null).content(content);
			if (rename != null)
			{
				builder.tag(rename);
			}
			return builder.build();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
