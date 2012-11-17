package com.vercer.leaf.convert;

import java.net.URL;

import com.vercer.convert.Converter;
import com.vercer.leaf.html.Anchor;

public class UriToAnchor implements Converter<URL, Anchor>
{
	@Override
	public Anchor convert(URL source)
	{
		return new Anchor(source.toExternalForm());
	}
}
