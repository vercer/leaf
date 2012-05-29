package com.vercer.jet.convert;

import java.net.URI;

import com.vercer.convert.Converter;
import com.vercer.jet.transform.Anchor;

public class UrlToAnchor implements Converter<URI, Anchor>
{
	@Override
	public Anchor convert(URI source)
	{
		return new Anchor(source.toASCIIString());
	}
}
