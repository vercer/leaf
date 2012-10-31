package com.vercer.jet.transform;

import com.vercer.jet.Markup;

public class CompositeTransformer implements Transformer
{
	private Transformer[] transformers;

	public CompositeTransformer(Transformer...transformers)
	{
		this.transformers = transformers;
	}
	
	@Override
	public Markup transform(Markup markup)
	{
		for (Transformer transformer : transformers)
		{
			markup = transformer.transform(markup);
		}
		return markup;
	}
}
