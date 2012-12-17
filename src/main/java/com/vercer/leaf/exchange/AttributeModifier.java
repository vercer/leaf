package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public class AttributeModifier extends Component
{
	private final String name;
	private final String value;

	public AttributeModifier(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	@Override
	public Markup exchangeComponent(Markup markup)
	{
		return Markup.builder(markup).attribute(name, modifyExisting(markup.getAttributes().get(name), value)).build();
	}

	protected String modifyExisting(String existing, String replacement)
	{
		return replacement;
	}
}
