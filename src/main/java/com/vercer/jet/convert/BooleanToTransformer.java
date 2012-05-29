package com.vercer.jet.convert;

import com.vercer.convert.Converter;
import com.vercer.jet.transform.Transformer;

public class BooleanToTransformer implements Converter<Boolean, Transformer>
{
	@Override
	public Transformer convert(final Boolean source)
	{
		if (source)
		{
			return Transformer.NO_OP;
		}
		else
		{
			return Transformer.REMOVE;
		}
	}
}
