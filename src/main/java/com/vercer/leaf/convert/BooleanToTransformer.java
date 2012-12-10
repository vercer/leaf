package com.vercer.leaf.convert;

import com.vercer.convert.Converter;
import com.vercer.leaf.exchange.Exchanger;
import com.vercer.leaf.exchange.ParentContainerExchanger;

public class BooleanToTransformer implements Converter<Boolean, Exchanger>
{
	@Override
	public Exchanger convert(final Boolean source)
	{
		if (source)
		{
			// use the parent container to transform any children
			return new ParentContainerExchanger();
		}
		else
		{
			return Exchanger.REMOVE;
		}
	}
}
