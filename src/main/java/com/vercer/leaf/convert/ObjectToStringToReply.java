package com.vercer.leaf.convert;

import com.google.inject.Inject;
import com.vercer.convert.Converter;
import com.vercer.convert.TypeConverter;
import com.vercer.leaf.Reply;

public class ObjectToStringToReply implements Converter<Object, Reply>
{
	private final TypeConverter converter;

	@Inject
	public ObjectToStringToReply(TypeConverter converter)
	{
		this.converter = converter;
	}

	@Override
	public Reply convert(Object input)
	{
		Object string = converter.convert(input, String.class);
		return converter.convert(string, Reply.class);
	}
}
