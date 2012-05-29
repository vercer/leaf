package com.vercer.jet.convert;

import com.vercer.convert.Converter;
import com.vercer.jet.transform.Label;

public class StringToLabel implements Converter<String, Label>
{
	@Override
	public Label convert(String source)
	{
		return new Label(source);
	}
}
