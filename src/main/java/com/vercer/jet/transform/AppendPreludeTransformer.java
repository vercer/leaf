package com.vercer.jet.transform;

import com.vercer.jet.Markup;
import com.vercer.jet.transform.Transformer;

public class AppendPreludeTransformer implements Transformer
{
	private final String separator;

	public AppendPreludeTransformer(String separator)
	{
		this.separator = separator;
	}

	@Override
	public Markup transform(Markup markup)
	{
		return Markup.builder(markup).prelude(markup.getPrelude() + separator).build();
	}

}
