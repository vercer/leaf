package com.vercer.jet.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.io.CharStreams;
import com.vercer.jet.Jet;
import com.vercer.jet.Markup;
import com.vercer.jet.Markup.Builder;
import com.vercer.jet.transform.Transformer;

public class InlineSourceTransformer implements Transformer
{
	private final boolean cache;
	private String content;
	private String attribute;
	private String rename;

	public InlineSourceTransformer()
	{
		this(true, "src");
	}

	public InlineSourceTransformer(boolean cache)
	{
		this(cache, "src");
	}

	public InlineSourceTransformer(boolean cache, String attribute)
	{
		this.cache = cache;
		this.attribute = attribute;
	}

	public InlineSourceTransformer rename(String rename)
	{
		this.rename = rename;
		return this;
	}

	@Override
	public Markup transform(Markup markup)
	{
		try
		{
			if (!cache || content == null)
			{
				String source = markup.getAttributes().get(attribute);
				InputStream stream = Jet.get().getServletContext().getResourceAsStream(source);
				content = CharStreams.toString(new InputStreamReader(stream));
			}

			Builder builder = Markup.builder(markup).clear().attribute(attribute, null).content(content);
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
