package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public class CompositeExchanger implements Exchanger
{
	private Exchanger[] transformers;

	public CompositeExchanger(Exchanger...transformers)
	{
		this.transformers = transformers;
	}
	
	@Override
	public Markup exchange(Markup markup)
	{
		for (Exchanger transformer : transformers)
		{
			markup = transformer.exchange(markup);
		}
		return markup;
	}
}
