package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public class AppendPrelude implements Exchanger
{
	private final String separator;

	public AppendPrelude(String separator)
	{
		this.separator = separator;
	}

	@Override
	public Markup exchange(Markup markup)
	{
		return Markup.builder(markup).prelude(markup.getPrelude() + separator).build();
	}

}
