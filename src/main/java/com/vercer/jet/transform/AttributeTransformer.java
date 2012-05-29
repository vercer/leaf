package com.vercer.jet.transform;

import com.google.inject.Provider;
import com.vercer.jet.Markup;

public class AttributeTransformer<T> extends Component<T>
{
	private final String name;

	public AttributeTransformer(String name, T value)
	{
		super(value);
		this.name = name;
	}

	public AttributeTransformer(String name, Provider<T> value)
	{
		super(value);
		this.name = name;
	}

	@Override
	public Markup transformComponent(Markup markup)
	{
		return Markup.builder(markup).attribute(name, modifyExisting(markup.getAttributes().get(name), valueToString(get()))).build();
	}

	protected String modifyExisting(String existing, String replacement)
	{
		return replacement;
	}

	protected String valueToString(T value)
	{
		return value == null ? "" : value.toString();
	}
}
