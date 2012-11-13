package com.vercer.leaf.convert;

import com.google.inject.Inject;
import com.vercer.convert.Converter;
import com.vercer.convert.TypeConverter;
import com.vercer.leaf.Response;

public class ObjectToResponse implements Converter<Object, Response>
{
	private final TypeConverter converter;

	@Inject
	public ObjectToResponse(TypeConverter converter)
	{
		this.converter = converter;
	}

	@Override
	public Response convert(Object input)
	{
		Object string = converter.convert(input, String.class);
		return converter.convert(string, Response.class);
	}
}
