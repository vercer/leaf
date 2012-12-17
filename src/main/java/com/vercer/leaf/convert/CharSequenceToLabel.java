package com.vercer.leaf.convert;

import com.vercer.convert.Converter;
import com.vercer.leaf.exchange.Label;

public class CharSequenceToLabel implements Converter<CharSequence, Label>
{
	@Override
	public Label convert(CharSequence source)
	{
		return new Label(source);
	}
}
