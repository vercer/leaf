package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public class RemoveAttribute implements Exchanger
{
	private String name;

	public RemoveAttribute(String name)
	{
		this.name = name;
	}
	
	@Override
	public Markup exchange(Markup markup)
	{
		return Markup.builder(markup).removeAttribute(name).build();
	}
}
