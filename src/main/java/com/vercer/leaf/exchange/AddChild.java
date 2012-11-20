package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public class AddChild implements Exchanger
{
	private Markup child;

	public AddChild(Markup child)
	{
		this.child = child;
	}

	@Override
	public Markup exchange(Markup markup)
	{
		return Markup.builder(markup).child(child).build();
	}
}
