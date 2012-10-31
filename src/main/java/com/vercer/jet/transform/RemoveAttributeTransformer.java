package com.vercer.jet.transform;

import com.vercer.jet.Markup;

public class RemoveAttributeTransformer implements Transformer
{
	private String name;

	public RemoveAttributeTransformer(String name)
	{
		this.name = name;
	}
	
	@Override
	public Markup transform(Markup markup)
	{
		return markup;
//		return Markup.builder(markup).removeAttribute(name).build();
	}
}
