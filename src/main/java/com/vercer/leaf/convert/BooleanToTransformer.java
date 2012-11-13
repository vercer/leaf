package com.vercer.leaf.convert;

import com.vercer.convert.Converter;
import com.vercer.leaf.exchange.Exchanger;

public class BooleanToTransformer implements Converter<Boolean, Exchanger>
{
	@Override
	public Exchanger convert(final Boolean source)
	{
		if (source)
		{
			return Exchanger.NO_OP;
		}
		else
		{
			return Exchanger.REMOVE;
		}
	}
}
