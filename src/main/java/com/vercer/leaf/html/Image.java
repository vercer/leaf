package com.vercer.leaf.html;

import java.net.URI;

import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;
import com.vercer.leaf.exchange.Component;

public class Image extends Component
{
	private String src;
	private final String title;

	public Image(String src)
	{
		this(src, null);
	}

	public Image(URI uri)
	{
		this(uri.toString(), null);
	}

	public Image(String src, String title)
	{
		this.src = src;
		this.title = title;
	}

	@Override
	public Markup exchangeComponent(Markup markup)
	{
		// add href and title attributes
		Builder builder = Markup.builder(markup).attribute("src", src);
		if (title != null)
		{
			builder.attribute("title", title);
		}

		return builder.build();
	}
}
