package com.vercer.jet.transform;

import com.google.inject.Provider;
import com.vercer.jet.Markup;

public class AttributeTransformer extends Component<String>
{
	private final String name;

	public AttributeTransformer(String name, String value)
	{
		super(value);
		this.name = name;
	}

	public AttributeTransformer(String name, Provider<String> value)
	{
		super(value);
		this.name = name;
	}

	@Override
	public Markup transformComponent(Markup markup)
	{
		return Markup.builder(markup).attribute(name, modifyExisting(markup.getAttributes().get(name), get())).build();
	}

	protected String modifyExisting(String existing, String replacement)
	{
		return replacement;
	}
}
