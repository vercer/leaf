package com.vercer.jet.convert;

import java.net.URL;

import com.vercer.convert.Converter;
import com.vercer.jet.transform.Anchor;

public class UriToAnchor implements Converter<URL, Anchor>
{
	@Override
	public Anchor convert(URL source)
	{
		return new Anchor(source.toExternalForm());
	}
}
