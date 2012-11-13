package com.vercer.leaf.exchange;

import com.google.inject.Provider;
import com.vercer.leaf.Markup;

public class AttributeModifier extends Component<String>
{
	private final String name;

	public AttributeModifier(String name, String value)
	{
		super(value);
		this.name = name;
	}

	public AttributeModifier(String name, Provider<String> value)
	{
		super(value);
		this.name = name;
	}

	@Override
	public Markup transformComponent(Markup markup)
	{
		String value = get();
		if (value == null)
		{
			return Exchanger.REMOVE.exchange(markup);
		}
		else
		{
			return Markup.builder(markup).attribute(name, modifyExisting(markup.getAttributes().get(name), value)).build();
		}
	}

	protected String modifyExisting(String existing, String replacement)
	{
		return replacement;
	}
}
