package com.vercer.leaf.convert;

import com.vercer.convert.Converter;
import com.vercer.leaf.exchange.Label;

public class StringToLabel implements Converter<String, Label>
{
	@Override
	public Label convert(String source)
	{
		return new Label(source);
	}
}
