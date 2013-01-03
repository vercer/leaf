package com.vercer.leaf;

import com.vercer.convert.Converter;
import com.vercer.leaf.exchange.BeanContainer;
import com.vercer.leaf.exchange.Container;

public class ObjectToContainer implements Converter<Object, Container>
{
	@Override
	public Container convert(Object source)
	{
		return new BeanContainer(source);
	}
}
