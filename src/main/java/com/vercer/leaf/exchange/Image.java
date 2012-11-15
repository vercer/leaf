package com.vercer.leaf.exchange;

import java.net.URI;

import com.google.inject.Provider;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;

public class Image extends Component<URI>
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

	public Image(Provider<URI> uri)
	{
		super(uri);
		this.title = null;
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
