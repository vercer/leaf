package com.vercer.leaf.convert;

import com.vercer.convert.Converter;
import com.vercer.leaf.Response;

public class StringToReply implements Converter<String, Response>
{
	@Override
	public Response convert(final String input)
	{
		return Response.withRedirect(input);
	}
}
